package com.uco;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class Proj3InfraApp {
    public static void main(final String[] args) {
        App app = new App();

        new Proj3InfraStack(app, "Proj3InfraStack");

        app.synth();
    }
}


