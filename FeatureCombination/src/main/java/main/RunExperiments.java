package main;

import java.io.IOException;

import com.technion.ir.trainers.ParamTrainer;

public class RunExperiments {

	public static void main(String[] args) throws IOException {
		
		ParamTrainer trainer;
		
		if (args.length < 3) {
			System.out.println("need to pass 3 arguments" + "\n" + "1.baseFeatureDirectory 2.takenFeatureDirectory" + "3.out path");
		}
		trainer = new  ParamTrainer(args[0], args[1], args[2]);
		trainer.combineFeautres();


	}

}
