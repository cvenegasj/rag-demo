package com.globant.ragdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@SpringBootApplication
@Slf4j
public class RagDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagDemoApplication.class, args);
	}

	@Bean
	public ApplicationRunner runner(
			MongoTemplate mongoTemplate,
			@Value("classpath:/docs/document-ley-universitaria.pdf") Resource pdfResource,
			VectorStore vectorStore) {
		return args -> {
			long count = mongoTemplate.estimatedCount("vector_store");

//			mongoTemplate.remove(new Query(), "vector_store");

			if (count > 0) {
				log.info("Current document count of vector_store collection is {}", count);
				log.info("Application is ready");
				return;
			}

			log.info("Loading documents to vector store...");

			PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
					.withPageExtractedTextFormatter(
							new ExtractedTextFormatter.Builder()
									.withNumberOfBottomTextLinesToDelete(0)
									.withNumberOfTopPagesToSkipBeforeDelete(0)
									.build())
					.withPagesPerDocument(1)
					.build();

			DocumentReader pdfReader = new PagePdfDocumentReader(pdfResource, config);
			TokenTextSplitter textSplitter = new TokenTextSplitter();
			List<Document> documents = textSplitter.apply(pdfReader.get());
			vectorStore.add(documents);

			log.info("Application is ready");
		};
	}

}
