package com.globant.ragdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RagService implements IRagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    public RagService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    @Override
    public String askLlm(String question) {
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(question).withTopK(3));
        String similarDocsContents = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));
        log.info("Similar documents contents found: \n{}", similarDocsContents);

        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
        Prompt prompt = promptTemplate.create(Map.of(
                "question", question,
                "context", similarDocsContents));

        String llmResponse = chatClient.prompt(prompt).call().content();
        return llmResponse;
    }
}
