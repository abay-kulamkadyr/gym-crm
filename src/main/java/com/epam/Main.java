package com.epam;

import com.epam.infrastructure.config.AppConfig;
import com.epam.interface_adapters.facade.GymFacade;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

	public static void main(String[] args) {
		var context = new AnnotationConfigApplicationContext(AppConfig.class);
		GymFacade facade = context.getBean(GymFacade.class);

		System.out.println("Trainees:");
		for (var trainee : facade.getAllTrainees()) {
			System.out.println(trainee);
		}

		System.out.println("Trainers:");
		for (var trainer : facade.getAllTrainers()) {
			System.out.println(trainer);
		}

		System.out.println("Trainings:");
		for (var training : facade.getAllTrainings()) {
			System.out.println(training);
		}

		System.out.println("TrainingTypes");
		for (var trainingType : facade.getAllTrainingTypes()) {
			System.out.println(trainingType);
		}
		context.close();
	}

}