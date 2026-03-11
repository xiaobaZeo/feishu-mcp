package com.feishu.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.config.FeishuProperties;
import com.feishu.mcp.constant.DocBlockType;
import com.feishu.mcp.dto.doc.*;
import com.feishu.mcp.exception.DocumentOperationException;
import com.feishu.mcp.exception.FeishuApiException;
import com.feishu.mcp.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 飞书云文档服务（重构版）
 * 使用 DTO 替代手动 JSON 拼接，支持多种文档操作
 */
@Slf4j
@Service
public class FeishuDocService {

    private final FeishuAuthService feishuAuthService;
    private final FeishuProperties feishuProperties;
    private final HttpClientUtil httpClient;
    private final ObjectMapper objectMapper;

    public FeishuDocService(FeishuAuthService feishuAuthService,
                            FeishuProperties feishuProperties,
                            HttpClientUtil httpClient,
                            ObjectMapper objectMapper) {
        this.feishuAuthService = feishuAuthService;
        this.feishuProperties = feishuProperties;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    // ==================== 文档搜索 ====================

    /**
     * 搜索云文档
     */
    public List<DocSearchResult> searchDocs(DocSearchRequest request) throws IOException {
        String url = buildUrl("/search/v1/quick");

        // 构建请求体
        Map<String, Object> body = new HashMap<>();
        body.put("query", request.getQuery());
        body.put("page_size", request.getPageSize());
        body.put("page", request.getPage());
        if (request.getCreator() != null && !request.getCreator().isEmpty()) {
            body.put("creator", request.getCreator());
        }

        FeishuApiResponse<SearchResponseData> response = httpClient.post(
                url,
                feishuAuthService.getAccessToken(),
                body,
                SearchResponseData.class
        );

        return convertSearchResults(response.getData());
    }

    /**
     * 搜索响应数据结构
     */
    private static class SearchResponseData {
        public List<JsonNode> items;
        public Integer total;
    }

    private List<DocSearchResult> convertSearchResults(SearchResponseData data) {
        List<DocSearchResult> results = new ArrayList<>();
        if (data == null || data.items == null) {
            return results;
        }

        for (JsonNode item : data.items) {
            DocSearchResult result = new DocSearchResult();
            result.setDocumentId(getText(item, "document_id"));
            result.setTitle(getText(item, "title"));
            result.setDocType(getText(item, "doc_type"));
            result.setUrl(getText(item, "url"));
            result.setCreator(getText(item, "creator"));
            result.setCreateTime(getText(item, "create_time"));
            result.setUpdateTime(getText(item, "update_time"));
            result.setSpaceName(getText(item, "space_name"));
            result.setNodeId(getText(item, "node_id"));
            results.add(result);
        }
        return results;
    }

    // ==================== 文档创建 ====================

    /**
     * 创建云文档
     */
    public DocCreateResponse createDoc(DocCreateRequest request) throws IOException {
        String url = buildUrl("/docx/v1/documents");

        // 构建请求体
        Map<String, Object> document = new HashMap<>();
        document.put("title", request.getTitle());

        // 处理内容
        if (request.getContentBlocks() != null && !request.getContentBlocks().isEmpty()) {
            // 使用结构化内容块
            document.put("content", request.getContentBlocks());
        } else if (request.getContent() != null && !request.getContent().isEmpty()) {
            // 使用纯文本内容，转换为块格式
            document.put("content", convertTextToBlocks(request.getContent()));
        }

        Map<String, Object> body = new HashMap<>();
        if (request.getNodeId() != null && !request.getNodeId().isEmpty()) {
            body.put("node_id", request.getNodeId());
        }
        body.put("document", document);

        try {
            FeishuApiResponse<CreateResponseData> response = httpClient.post(
                    url,
                    feishuAuthService.getAccessToken(),
                    body,
                    CreateResponseData.class
            );

            return DocCreateResponse.builder()
                    .success(true)
                    .documentId(getText(response.getData().document, "document_id"))
                    .token(getText(response.getData().document, "document_token"))
                    .url(getText(response.getData().document, "url"))
                    .title(request.getTitle())
                    .createTime(System.currentTimeMillis())
                    .build();
        } catch (FeishuApiException e) {
            return DocCreateResponse.builder()
                    .success(false)
                    .title(request.getTitle())
                    .errorMessage(e.getApiMessage())
                    .build();
        }
    }

    private static class CreateResponseData {
        public JsonNode document;
    }

    /**
     * 将纯文本转换为飞书文档块格式
     */
    private List<List<Map<String, Object>>> convertTextToBlocks(String text) {
        List<List<Map<String, Object>>> blocks = new ArrayList<>();

        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }

            List<Map<String, Object>> block = new ArrayList<>();
            Map<String, Object> element = new HashMap<>();
            element.put("tag", "text");
            element.put("text", line);
            block.add(element);
            blocks.add(block);
        }

        return blocks;
    }

    // ==================== 文档获取 ====================

    /**
     * 获取云文档基本信息
     */
    public DocContent getDoc(String documentId) throws IOException {
        String url = buildUrl("/docx/v1/documents/" + documentId);

        FeishuApiResponse<JsonNode> response = httpClient.get(
                url,
                feishuAuthService.getAccessToken(),
                JsonNode.class
        );

        JsonNode docNode = response.getData().get("document");

        return DocContent.builder()
                .documentId(getText(docNode, "document_id"))
                .title(getText(docNode, "title"))
                .revision(getLong(docNode, "revision"))
                .build();
    }

    /**
     * 获取云文档完整内容（包括块）
     */
    public DocContent getDocWithBlocks(String documentId) throws IOException {
        // 1. 获取文档基本信息
        DocContent docContent = getDoc(documentId);

        // 2. 获取文档块列表
        List<DocBlock> blocks = getDocBlocks(documentId, 500);
        docContent.setBlocks(blocks);

        return docContent;
    }

    /**
     * 获取文档块列表
     */
    public List<DocBlock> getDocBlocks(String documentId, int pageSize) throws IOException {
        String url = buildUrl("/docx/v1/documents/" + documentId + "/blocks?page_size=" + pageSize);

        FeishuApiResponse<BlockListData> response = httpClient.get(
                url,
                feishuAuthService.getAccessToken(),
                BlockListData.class
        );

        return convertBlocks(response.getData());
    }

    private static class BlockListData {
        public List<JsonNode> items;
    }

    private List<DocBlock> convertBlocks(BlockListData data) {
        List<DocBlock> blocks = new ArrayList<>();
        if (data == null || data.items == null) {
            return blocks;
        }

        for (JsonNode item : data.items) {
            DocBlockType blockType = DocBlockType.fromCode(getInt(item, "block_type", 0));

            DocBlock block = DocBlock.builder()
                    .blockId(getText(item, "block_id"))
                    .parentId(getText(item, "parent_id"))
                    .blockType(blockType)
                    .blockTypeCode(getInt(item, "block_type", 0))
                    .childrenCount(getInt(item, "children_count", 0))
                    .content(extractBlockText(item))
                    .build();

            blocks.add(block);
        }
        return blocks;
    }

    /**
     * 从块中提取文本内容
     */
    private String extractBlockText(JsonNode block) {
        // 尝试从不同块类型中提取文本
        String[] blockTypeFields = {"text", "heading1", "heading2", "heading3", "heading4",
                "heading5", "heading6", "heading7", "heading8", "heading9",
                "bulleted_list", "numbered_list", "code", "quote"};

        for (String field : blockTypeFields) {
            if (block.has(field) && block.get(field).has("elements")) {
                return extractTextFromElements(block.get(field).get("elements"));
            }
        }
        return "";
    }

    private String extractTextFromElements(JsonNode elements) {
        StringBuilder text = new StringBuilder();
        if (elements == null || !elements.isArray()) {
            return "";
        }

        for (JsonNode element : elements) {
            if (element.has("text_run") && element.get("text_run").has("content")) {
                text.append(element.get("text_run").get("content").asText());
            }
        }
        return text.toString();
    }

    // ==================== 文档更新 ====================

    /**
     * 根据多种定位方式更新文档
     */
    public DocUpdateResponse updateDocContent(String documentId, String text,
                                               String operation, DocumentLocation location) throws IOException {
        // 1. 解析定位信息，获取目标块ID和是否为page块
        BlockResolutionResult resolution = resolveBlockIdWithType(documentId, location);

        if (resolution == null || resolution.getBlockId() == null) {
            return DocUpdateResponse.error("无法解析定位信息: " + location.getLocationDescription(), null);
        }

        String targetBlockId = resolution.getBlockId();
        boolean isPageBlock = resolution.isPageBlock();

        // 2. 根据操作类型构建请求
        List<DocBlockUpdate> requests = buildUpdateRequests(targetBlockId, text, operation, documentId, isPageBlock);

        // 3. 执行更新
        DocUpdateRequest updateRequest = DocUpdateRequest.builder()
                .documentId(documentId)
                .requests(requests)
                .build();

        return updateDoc(documentId, updateRequest);
    }

    /**
     * 更新云文档
     */
    public DocUpdateResponse updateDoc(String documentId, DocUpdateRequest request) throws IOException {
        String url = buildUrl("/docx/v1/documents/" + documentId + "/blocks/batch_update");

        // 获取文档当前 revision
        DocContent docContent = getDoc(documentId);
        Long revisionId = docContent.getRevision();

        // 构建请求体
        List<Map<String, Object>> requests = new ArrayList<>();
        for (DocBlockUpdate update : request.getRequests()) {
            Map<String, Object> req = new HashMap<>();
            req.put("block_id", update.getBlockId());

            switch (update.getOperationType()) {
                case "update_text_elements" -> {
                    Map<String, Object> updateText = new HashMap<>();
                    List<Map<String, Object>> elements = new ArrayList<>();

                    for (DocElement element : update.getElements()) {
                        Map<String, Object> el = new HashMap<>();
                        el.put("tag", element.getTag());

                        if ("text".equals(element.getTag())) {
                            Map<String, Object> textRun = new HashMap<>();
                            textRun.put("content", element.getText());
                            el.put("text_run", textRun);
                        }
                        elements.add(el);
                    }

                    updateText.put("elements", elements);
                    req.put("update_text_elements", updateText);
                }
                case "insert_block_children" -> {
                    // 使用单独的API在page块下添加子块
                    // /docx/v1/documents/{document_id}/blocks/{block_id}/children
                    String insertUrl = buildUrl("/docx/v1/documents/" + documentId +
                            "/blocks/" + update.getBlockId() + "/children");

                    // 构建要插入的子块
                    List<Map<String, Object>> children = new ArrayList<>();
                    for (DocElement element : update.getElements()) {
                        Map<String, Object> child = new HashMap<>();
                        child.put("block_type", 2); // text block

                        Map<String, Object> textBlock = new HashMap<>();
                        List<Map<String, Object>> childElements = new ArrayList<>();

                        Map<String, Object> textRun = new HashMap<>();
                        textRun.put("content", element.getText());

                        Map<String, Object> el = new HashMap<>();
                        el.put("text_run", textRun);

                        childElements.add(el);
                        textBlock.put("elements", childElements);
                        child.put("text", textBlock);

                        children.add(child);
                    }

                    Map<String, Object> insertBody = new HashMap<>();
                    insertBody.put("children", children);
                    if (update.getInsertIndex() != null) {
                        insertBody.put("index", update.getInsertIndex());
                    }

                    log.debug("Insert children request body: {}", objectMapper.writeValueAsString(insertBody));

                    FeishuApiResponse<JsonNode> insertResponse = httpClient.post(
                            insertUrl,
                            feishuAuthService.getAccessToken(),
                            insertBody,
                            JsonNode.class
                    );

                    log.debug("Insert children response: {}", insertResponse);

                    // 跳过这个请求，因为我们已经处理了
                    continue;
                }
                default -> throw new IllegalArgumentException("不支持的操作类型: " + update.getOperationType());
            }

            requests.add(req);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("requests", requests);
        // 使用 -1 表示最新版本，或传入具体的 revision_id
        body.put("document_revision_id", request.getRevisionId() != null ? request.getRevisionId() : -1);

        try {
            FeishuApiResponse<UpdateResponseData> response = httpClient.patch(
                    url,
                    feishuAuthService.getAccessToken(),
                    body,
                    UpdateResponseData.class
            );

            return DocUpdateResponse.success(
                    request.getRequests().get(0).getBlockId(),
                    request.getRequests().get(0).getOperationType(),
                    response.getData() != null ? response.getData().revisionId : null
            );
        } catch (FeishuApiException e) {
            return DocUpdateResponse.error(e.getApiMessage(), e.getMessage());
        }
    }

    private static class UpdateResponseData {
        public Long revisionId;
    }

    /**
     * 解析定位信息，获取目标块ID和块类型
     */
    private BlockResolutionResult resolveBlockIdWithType(String documentId, DocumentLocation location) throws IOException {
        // 优先级 1：直接指定 blockId
        if (location.hasBlockId()) {
            return new BlockResolutionResult(location.getBlockId(), false);
        }

        // 获取文档块列表（排除 page 块）
        List<DocBlock> blocks = getDocBlocks(documentId, 500);
        List<DocBlock> editableBlocks = blocks.stream()
                .filter(b -> !b.isPage())
                .toList();

        // 优先级 2：按 blockIndex 定位
        if (location.hasBlockIndex()) {
            int index = location.getBlockIndex();
            if (index < 0 || index >= editableBlocks.size()) {
                throw DocumentOperationException.blockIndexOutOfRange(index, editableBlocks.size() - 1);
            }
            return new BlockResolutionResult(editableBlocks.get(index).getBlockId(), false);
        }

        // 优先级 3：按 blockType 定位（返回第一个匹配的块）
        if (location.hasBlockType()) {
            String blockId = editableBlocks.stream()
                    .filter(b -> b.getBlockType() == location.getBlockType())
                    .findFirst()
                    .map(DocBlock::getBlockId)
                    .orElseThrow(() -> DocumentOperationException.blockTypeNotFound(location.getBlockType().name()));
            return new BlockResolutionResult(blockId, false);
        }

        // 优先级 4：按 positionHint 定位（简单启发式）
        if (location.hasPositionHint()) {
            return resolveByPositionHintWithType(blocks, editableBlocks, location.getPositionHint());
        }

        throw DocumentOperationException.invalidLocation();
    }

    /**
     * 块解析结果
     */
    private static class BlockResolutionResult {
        private final String blockId;
        private final boolean pageBlock;

        public BlockResolutionResult(String blockId, boolean pageBlock) {
            this.blockId = blockId;
            this.pageBlock = pageBlock;
        }

        public String getBlockId() {
            return blockId;
        }

        public boolean isPageBlock() {
            return pageBlock;
        }
    }

    /**
     * 根据位置描述解析块ID（简单启发式实现）
     * 当文档为空时，返回 page 块ID以便添加新内容
     */
    private BlockResolutionResult resolveByPositionHintWithType(List<DocBlock> allBlocks, List<DocBlock> editableBlocks, String hint) {
        String normalized = hint.toLowerCase().trim();

        // 如果文档为空（没有可编辑块），返回 page 块的ID
        DocBlock pageBlock = allBlocks.stream()
                .filter(DocBlock::isPage)
                .findFirst()
                .orElse(null);

        if (editableBlocks.isEmpty()) {
            if (pageBlock != null) {
                return new BlockResolutionResult(pageBlock.getBlockId(), true);
            }
            return null;
        }

        // "开头"、"第一" -> 第一个块
        if (normalized.contains("开头") || normalized.contains("第一") || normalized.contains("first")) {
            return new BlockResolutionResult(editableBlocks.get(0).getBlockId(), false);
        }

        // "末尾"、"最后"、"结尾" -> 最后一个块
        if (normalized.contains("末尾") || normalized.contains("最后") || normalized.contains("结尾")
                || normalized.contains("last") || normalized.contains("end")) {
            return new BlockResolutionResult(editableBlocks.get(editableBlocks.size() - 1).getBlockId(), false);
        }

        // "标题" -> 第一个标题块
        if (normalized.contains("标题") || normalized.contains("heading")) {
            String blockId = editableBlocks.stream()
                    .filter(DocBlock::isHeading)
                    .findFirst()
                    .map(DocBlock::getBlockId)
                    .orElseThrow(() -> DocumentOperationException.blockTypeNotFound("heading"));
            return new BlockResolutionResult(blockId, false);
        }

        // 默认返回第一个块
        return new BlockResolutionResult(editableBlocks.get(0).getBlockId(), false);
    }

    /**
     * 构建更新请求列表
     * @param blockId 目标块ID
     * @param text 文本内容
     * @param operation 操作类型
     * @param documentId 文档ID
     * @param isPageBlock 是否定位到 page 块（用于判断是否需要添加子块）
     */
    private List<DocBlockUpdate> buildUpdateRequests(String blockId, String text, String operation,
                                                      String documentId, boolean isPageBlock) throws IOException {
        List<DocBlockUpdate> requests = new ArrayList<>();

        // 如果是 page 块，使用 insert_block_children 添加子块
        if (isPageBlock) {
            String[] lines = text.split("\n");
            List<DocElement> elements = new ArrayList<>();
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    elements.add(DocElement.text(line));
                }
            }
            requests.add(DocBlockUpdate.insertBlock(blockId, elements, null));
            return requests;
        }

        switch (operation.toLowerCase()) {
            case "replace" -> {
                // 直接替换内容
                requests.add(DocBlockUpdate.updateText(blockId, text));
            }
            case "insert" -> {
                // insert 操作：获取现有内容，追加新内容
                String existingContent = getBlockContent(documentId, blockId);
                String newContent = existingContent + text;
                requests.add(DocBlockUpdate.updateText(blockId, newContent));
            }
            case "append" -> {
                // append 操作：在文档末尾追加新块（简化实现：替换最后一个块）
                requests.add(DocBlockUpdate.updateText(blockId, text));
            }
            default -> throw new IllegalArgumentException("不支持的操作类型: " + operation);
        }

        return requests;
    }

    /**
     * 获取指定块的内容
     */
    private String getBlockContent(String documentId, String blockId) throws IOException {
        String url = buildUrl("/docx/v1/documents/" + documentId + "/blocks/" + blockId);

        try {
            FeishuApiResponse<JsonNode> response = httpClient.get(
                    url,
                    feishuAuthService.getAccessToken(),
                    JsonNode.class
            );

            JsonNode block = response.getData().get("block");
            return extractBlockText(block);
        } catch (Exception e) {
            log.warn("获取块内容失败: documentId={}, blockId={}", documentId, blockId, e);
            return "";
        }
    }

    // ==================== 文档清空 ====================

    /**
     * 清空文档内容
     */
    public DocClearResponse clearDoc(String documentId) throws IOException {
        // 1. 获取文档块列表
        List<DocBlock> blocks = getDocBlocks(documentId, 500);

        // 2. 找到 page 块和所有可编辑的子块
        DocBlock pageBlock = blocks.stream()
                .filter(DocBlock::isPage)
                .findFirst()
                .orElseThrow(DocumentOperationException::pageBlockNotFound);

        String pageBlockId = pageBlock.getBlockId();

        // 3. 获取所有非 page 的子块（这些是要删除的）
        List<DocBlock> childBlocks = blocks.stream()
                .filter(b -> !b.isPage() && b.getParentId() != null && b.getParentId().equals(pageBlockId))
                .toList();

        int childrenCount = childBlocks.size();

        // 如果没有子块，直接返回成功
        if (childrenCount == 0) {
            log.info("文档 {} 没有可删除的内容块", documentId);
            return DocClearResponse.empty();
        }

        log.info("准备清空文档 {}，找到 {} 个内容块", documentId, childrenCount);

        // 4. 调用 batch_delete 删除 page 块下的所有子块
        // 使用 start_index=0, end_index=childrenCount 删除全部
        String deleteUrl = buildUrl("/docx/v1/documents/" + documentId +
                "/blocks/" + pageBlockId +
                "/children/batch_delete");

        Map<String, Object> body = new HashMap<>();
        body.put("start_index", 0);
        body.put("end_index", childrenCount);

        log.debug("清空文档请求: url={}, body={}", deleteUrl, objectMapper.writeValueAsString(body));

        try {
            FeishuApiResponse<ClearResponseData> response = httpClient.delete(
                    deleteUrl,
                    feishuAuthService.getAccessToken(),
                    body,
                    ClearResponseData.class
            );

            return DocClearResponse.success(
                    childrenCount,
                    pageBlockId,
                    response.getData() != null ? response.getData().revisionId : null
            );
        } catch (FeishuApiException e) {
            log.error("清空文档失败: {}", e.getApiMessage(), e);
            return DocClearResponse.error(e.getApiMessage());
        }
    }

    private static class ClearResponseData {
        public Long revisionId;
    }

    // ==================== 评论相关 ====================

    /**
     * 获取文档评论列表
     * 使用 /drive/v1/files/{file_token}/comments API
     */
    public List<DocComment> getDocComments(String documentId) throws IOException {
        String url = buildUrl("/drive/v1/files/" + documentId + "/comments?file_type=docx");

        FeishuApiResponse<JsonNode> response = httpClient.get(
                url,
                feishuAuthService.getAccessToken(),
                JsonNode.class
        );

        log.debug("评论列表API响应: {}", objectMapper.writeValueAsString(response.getData()));

        return convertDriveComments(response.getData());
    }

    /**
     * 转换 drive API 的评论列表
     */
    private List<DocComment> convertDriveComments(JsonNode data) {
        List<DocComment> comments = new ArrayList<>();
        if (data == null || !data.has("items")) {
            return comments;
        }

        JsonNode items = data.get("items");
        for (JsonNode item : items) {
            DocComment comment = new DocComment();
            comment.setCommentId(getText(item, "comment_id"));
            comment.setContent(extractCommentContent(item));
            comment.setCreatedBy(getText(item, "user_id"));
            comment.setCreateTime(getText(item, "create_time"));
            comment.setQuote(getText(item, "quote"));
            comments.add(comment);
        }
        return comments;
    }

    /**
     * 从评论数据中提取评论内容
     */
    private String extractCommentContent(JsonNode item) {
        if (item.has("reply_list") && item.get("reply_list").has("replies")) {
            JsonNode replies = item.get("reply_list").get("replies");
            if (replies.isArray() && replies.size() > 0) {
                JsonNode firstReply = replies.get(0);
                if (firstReply.has("content") && firstReply.get("content").has("elements")) {
                    JsonNode elements = firstReply.get("content").get("elements");
                    StringBuilder content = new StringBuilder();
                    for (JsonNode element : elements) {
                        if (element.has("text_run") && element.get("text_run").has("text")) {
                            content.append(element.get("text_run").get("text").asText());
                        }
                    }
                    return content.toString();
                }
            }
        }
        return null;
    }

    /**
     * 添加文档评论
     * 使用 /drive/v1/files/{file_token}/comments API
     */
    public DocComment addDocComment(String documentId, String content, String quote) throws IOException {
        // 使用 drive API 添加评论
        String url = buildUrl("/drive/v1/files/" + documentId + "/comments?file_type=docx");

        // 构建请求体 - 按照飞书API文档格式
        Map<String, Object> textRun = new HashMap<>();
        textRun.put("text", content);

        Map<String, Object> element = new HashMap<>();
        element.put("type", "text_run");
        element.put("text_run", textRun);

        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(element);

        Map<String, Object> replyContent = new HashMap<>();
        replyContent.put("elements", elements);

        Map<String, Object> reply = new HashMap<>();
        reply.put("content", replyContent);

        List<Map<String, Object>> replies = new ArrayList<>();
        replies.add(reply);

        Map<String, Object> replyList = new HashMap<>();
        replyList.put("replies", replies);

        Map<String, Object> body = new HashMap<>();
        body.put("reply_list", replyList);

        try {
            FeishuApiResponse<JsonNode> response = httpClient.post(
                    url,
                    feishuAuthService.getAccessToken(),
                    body,
                    JsonNode.class
            );

            JsonNode data = response.getData();
            if (data == null) {
                throw new IOException("添加评论失败：API响应中没有数据");
            }

            DocComment result = new DocComment();
            result.setCommentId(getText(data, "comment_id"));
            result.setContent(content);
            result.setQuote(quote);
            result.setCreatedBy(getText(data, "user_id"));
            result.setCreateTime(getText(data, "create_time"));
            return result;
        } catch (FeishuApiException e) {
            throw new IOException("添加评论失败: " + e.getApiMessage(), e);
        }
    }

    // ==================== 辅助方法 ====================

    private String buildUrl(String path) {
        return feishuProperties.getApiBaseUrl() + path;
    }

    private String buildDocumentUrl(String token) {
        return "https://open.feishu.cn/document/" + token;
    }

    private String getText(JsonNode node, String field) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return null;
    }

    private Long getLong(JsonNode node, String field) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asLong();
        }
        return null;
    }

    private int getInt(JsonNode node, String field, int defaultValue) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asInt();
        }
        return defaultValue;
    }

    private boolean getBoolean(JsonNode node, String field, boolean defaultValue) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asBoolean();
        }
        return defaultValue;
    }
}
