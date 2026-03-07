package com.epam.application.messaging.publisher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.epam.application.messaging.event.TrainerWorkloadEvent;
import com.epam.domain.model.Training;
import com.epam.infrastructure.logging.MdcConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrainingEventPublisher {

    private final KafkaTemplate<String, TrainerWorkloadEvent> kafkaTemplate;
    private final String trainingCreatedTopic;

    @Autowired
    public TrainingEventPublisher(
            KafkaTemplate<String, TrainerWorkloadEvent> kafkaTemplate,
            @Value("${app.kafka.topics.training-created}") String trainingCreatedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.trainingCreatedTopic = trainingCreatedTopic;
    }

    public void publishTrainingCreated(TrainerWorkloadEvent event) {
        String transactionId = MDC.get(MdcConstants.TRANSACTION_ID_MDC_KEY);
        event.setTransactionId(transactionId);

        log.debug("Publishing training created event for trainer: {}", event.getTrainerUsername());

        CompletableFuture<SendResult<String, TrainerWorkloadEvent>> future =
                kafkaTemplate.send(trainingCreatedTopic, event.getTrainerUsername(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                        "Successfully published training event for trainer: {} to partition: {} [TxnId: {}]",
                        event.getTrainerUsername(),
                        result.getRecordMetadata().partition(),
                        transactionId);
            } else {
                log.error(
                        "Failed to publish training event for trainer: {} [TxnId: {}]",
                        event.getTrainerUsername(),
                        transactionId,
                        ex);
            }
        });
    }

    public void publishTrainingDeleted(TrainerWorkloadEvent event) {
        String transactionId = MDC.get(MdcConstants.TRANSACTION_ID_MDC_KEY);
        event.setTransactionId(transactionId);
        event.setActionType(TrainerWorkloadEvent.ActionType.DELETE);

        log.debug("Publishing training deleted event for trainer: {}", event.getTrainerUsername());

        CompletableFuture<SendResult<String, TrainerWorkloadEvent>> future =
                kafkaTemplate.send(trainingCreatedTopic, event.getTrainerUsername(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published training deletion event for trainer: {}", event.getTrainerUsername());
            } else {
                log.error("Failed to publish training deletion event for trainer: {}", event.getTrainerUsername(), ex);
            }
        });
    }

    public void publishDeleteEventsForTrainings(List<Training> trainings) {
        for (Training training : trainings) {
            TrainerWorkloadEvent event = TrainerWorkloadEvent.builder()
                    .trainerUsername(training.getTrainer().getUsername())
                    .trainerFirstname(training.getTrainer().getFirstName())
                    .trainerLastname(training.getTrainer().getLastName())
                    .isActive(training.getTrainer().getActive())
                    .trainingDate(training.getTrainingDate())
                    .trainingDurationMinutes(training.getTrainingDurationMin())
                    .actionType(TrainerWorkloadEvent.ActionType.DELETE)
                    .build();

            publishTrainingDeleted(event);
        }
    }
}
