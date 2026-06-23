package ru.kutkovmax.jfxtu4;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

record Instruction(int q1, String a, String v, int q2){}

class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {


        List<Instruction> instrList = new ArrayList<Instruction>();
        Map<String, Integer> stateToId = new HashMap<>();



        Text title = new Text("Эмулятор машины Тьюринга в четвёрках, v0.0.1");
        Button helpButton = new Button("?");
        HBox titleBox = new HBox(title,helpButton);
        TextField tapeInput = new TextField();
        Button startButton = new Button("Старт");

        HBox controlBox = new HBox(startButton);
        TextArea programInput = new TextArea();
        VBox mainContentBox = new VBox(titleBox, tapeInput, controlBox,programInput);
        Scene scene = new Scene(mainContentBox);
        stage.setScene(scene);
        stage.setWidth (300);
        stage.setHeight(300);
        stage.show();

        startButton.setOnAction((event) -> {
            String[] instructions = programInput.getText().split("\n");

            for (String instruction: instructions) {
                if (instruction.isEmpty()) {
                    continue;
                }
                String[] fuck = instruction.split(",");
                if (fuck.length != 4) {
                    continue; // error
                }

                int q1 = stateToId.computeIfAbsent(fuck[0], k -> stateToId.size());
                String a = fuck[1];
                String v = fuck[2];
                int q2 = stateToId.computeIfAbsent(fuck[3], k -> stateToId.size());
                instrList.add(new Instruction(q1, a, v, q2));
            }

            for (Instruction instr : instrList){
                System.out.println(instr.q1() + " " + instr.a() + " " + instr.v() + " " + instr.q2());
            }


            int state = stateToId.get("0");
            ArrayList<String> tape = new ArrayList<String>();
//            tape.add(" ");
            tapeInput.getText().chars()
                    .mapToObj(c -> String.valueOf((char) c))
                    .forEach(tape::add);
            int head = tapeInput.getText().length() -1;
            // loop
            boolean isFinish = false;
            while (!isFinish){

                System.out.print("\n");
                for (String cell : tape){
                    System.out.print(cell + " ");
                }

                String symbol = tape.get(head);
                for (Instruction instr : instrList){
                    if (instr.q1() == state && instr.a().equals(symbol)){
                        if (instr.v().equals(">")){
                            head++;
                        }else if (instr.v().equals("<")){
                            head--;
                        }else if(instr.v().equals("#")){
                            isFinish = true;
                            break;
                        }else{
                            tape.set(head, instr.v());
                        }
                        state = instr.q2();
                        break;
                    }
                }
                tapeInput.setText(String.join("", tape));
            }
            // loop
        });
    }

}