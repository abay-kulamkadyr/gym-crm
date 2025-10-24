package com.epam;

import com.epam.config.AppConfig;
import com.epam.facade.GymFacade;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

	public static void main(String[] args) {
		var context = new AnnotationConfigApplicationContext(AppConfig.class);
		GymFacade facade = context.getBean(GymFacade.class);

		System.out.println("Trainees: ");
		for (var trainee : facade.getAllTrainees()) {
			System.out.println(trainee);
		}
		context.close();
	}

}