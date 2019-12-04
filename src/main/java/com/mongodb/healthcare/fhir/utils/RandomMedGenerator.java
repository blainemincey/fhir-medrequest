package com.mongodb.healthcare.fhir.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class RandomMedGenerator {

    private Random random = new Random();
    private List<Medicine> medicines = new ArrayList<>();

    public RandomMedGenerator() {
        this.createMedList();
    }

    /**
     *
     */
    private void createMedList(){
        Medicine med1 = new Medicine("Hydrocodone", "Pain", "Once a day");
        Medicine med2 = new Medicine("Lipitor", "High Cholesterol", "Twice a day");
        Medicine med3 = new Medicine("Lisonopril", "High Blood Pressure", "Once before bed");
        Medicine med4 = new Medicine("Ambien", "Sleep Disorder", "Once before bed");
        Medicine med5 = new Medicine("Motrin", "Fever", "Every 4 hours");
        Medicine med6 = new Medicine("Zofran", "Nausea", "As needed");
        Medicine med7 = new Medicine("Metformin", "Diabetes", "Three times daily");
        Medicine med8 = new Medicine("Amoxicillin", "Infection", "Twice daily with food");
        Medicine med9 = new Medicine("Prednisone", "Arthritis", "Once daily with food");

        medicines.add(med1);
        medicines.add(med2);
        medicines.add(med3);
        medicines.add(med4);
        medicines.add(med5);
        medicines.add(med6);
        medicines.add(med7);
        medicines.add(med8);
        medicines.add(med9);

    }

    /**
     *
     * @return
     */
    public Medicine getMedicine(){
        return medicines.get(random.nextInt(medicines.size()));
    }

    // Inner class for Medicine
    public class Medicine {
        private String name;
        private String condition;
        private String dosage;

        public Medicine(String name, String condition, String dosage){
            this.name = name;
            this.condition = condition;
            this.dosage = dosage;
        }

        public String getName() {
            return name;
        }

        public String getCondition() {
            return condition;
        }

        public String getDosage() {
            return dosage;
        }

        @Override
        public String toString() {
            return "Medicine{" +
                    "name='" + name + '\'' +
                    ", condition='" + condition + '\'' +
                    ", dosage='" + dosage + '\'' +
                    '}';
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Starting RandomMedGenerator.");

        RandomMedGenerator medGenerator = new RandomMedGenerator();
        for(int idx = 0; idx < 25; idx++) {
            System.out.println(medGenerator.getMedicine());
        }

        System.out.println("Stopping RandomMedGenerator.");
    }
}
