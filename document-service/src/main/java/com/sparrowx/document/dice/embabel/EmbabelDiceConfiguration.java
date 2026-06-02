//package com.sparrowx.document.dice.embabel;
//
//import com.embabel.agent.api.common.Ai;
//import com.embabel.agent.core.DataDictionary;
//import com.embabel.common.ai.model.LlmOptions;
//import com.embabel.dice.common.SchemaAdherence;
//import com.embabel.dice.pipeline.PropositionPipeline;
//import com.embabel.dice.proposition.PropositionExtractor;
//import com.embabel.dice.proposition.extraction.LlmPropositionExtractor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class EmbabelDiceConfiguration {
//
//    @Bean
//    public DataDictionary documentEvidenceDataDictionary() {
//        return DataDictionary.fromClasses(
//                "sparrowx-document-evidence",
//                DocumentEvidenceFact.class,
//                DocumentEvidenceActor.class,
//                DocumentEvidenceObject.class
//        );
//    }
//
//    @Bean
//    public PropositionExtractor documentPropositionExtractor(
//            Ai ai,
//            LlmOptions llmOptions
//    ) {
//        return LlmPropositionExtractor
//                .withLlm(llmOptions)
//                .withAi(ai)
//                .withSchemaAdherence(SchemaAdherence.DEFAULT)
//                .withTemplate("dice/extract_document_evidence_propositions");
//    }
//
//    @Bean
//    public PropositionPipeline documentPropositionPipeline(
//            PropositionExtractor documentPropositionExtractor
//    ) {
//        return PropositionPipeline.withExtractor(documentPropositionExtractor);
//    }
//}