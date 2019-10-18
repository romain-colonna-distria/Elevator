package fr.univ_amu.strategy;

import fr.univ_amu.utils.Constant;
import fr.univ_amu.utils.Direction;

import java.util.List;

//TOdo: fnir strategie
public class MinimumStrategy implements SatisfactionStrategy {

    @SuppressWarnings("Duplicates")
    @Override
    public synchronized void orderRequest(List<Short> waitingLine, short targetFloor, short currentFloor, Direction directionAfterReachingTargetFloor, Direction actualDirection) {
        if(actualDirection.equals(Direction.STAY) && waitingLine.isEmpty()){
            System.out.println("stay : ");
            if(directionAfterReachingTargetFloor.equals(Direction.STAY)){
                System.out.println("stay : ");
                if(targetFloor == currentFloor){
                    System.out.println("=");
                    return; //l'ascenseur ne bouge pas et quelqu'un appui sur le numero de l'étage depuis l'intérieur
                }
                else if(targetFloor > currentFloor){
                    System.out.println(">");
                    waitingLine.add(targetFloor);
                }
                else {//targetFloor < currentFloor.get()
                    System.out.println("<");
                    waitingLine.add(targetFloor);
                }
            }
            else if(directionAfterReachingTargetFloor.equals(Direction.UP)){
                System.out.println("up : ");
                if(targetFloor == currentFloor){
                    System.out.println("=");
                    return; //l'ascenseur ne bouge pas et quelqu'un l'appel depuis le meme etage depuis l'extérieur pour monter
                }
                else if(targetFloor > currentFloor){
                    System.out.println(">");
                    waitingLine.add(targetFloor);
                }
                else {//targetFloor < currentFloor.get()
                    System.out.println("<");
                    waitingLine.add(targetFloor);
                }
            }
            else if(directionAfterReachingTargetFloor.equals(Direction.DOWN)){
                System.out.println("down : ");
                if(targetFloor == currentFloor){
                    System.out.println("=");
                    return; //l'ascenseur ne bouge pas et quelqu'un l'appel depuis le meme etage depuis l'extérieur pour descendre
                }
                else if(targetFloor > currentFloor){
                    System.out.println(">");
                    waitingLine.add(targetFloor);
                }
                else {//targetFloor < currentFloor.get()
                    System.out.println("<");
                    waitingLine.add(targetFloor);
                }
            }
            else {
                System.out.println("?");
            }
        }
        else if(actualDirection.equals(Direction.UP)){
            System.out.println("up : ");
            if (directionAfterReachingTargetFloor.equals(Direction.STAY)) {
                System.out.println("stay : ");
                if (targetFloor == currentFloor) {
                    System.out.println("=");
                    return; //si on clique depuis l'interieur de l'ascenseur sur l'etage courant pendant une monté on fait rien
                }
                else if (targetFloor > currentFloor) {
                    System.out.println(">");
                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) > targetFloor) {
                            waitingLine.add(i, targetFloor);
                            isAdded = true;
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else {//targetFloor < currentFloor.get()
                    System.out.println("<");
                    short max = 0;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        max = max > waitingLine.get(i) ? max : waitingLine.get(i);

                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == max) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) < targetFloor) {
                                    waitingLine.add(j, targetFloor);
                                    isAdded = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
            }
            else if (directionAfterReachingTargetFloor.equals(Direction.UP)) {
                System.out.println("up : ");
                if (targetFloor == currentFloor) {
                    System.out.println("=");
                    short max = Constant.FLOOR_MIN;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        max = max > waitingLine.get(i) ? max : waitingLine.get(i);

                    short min = Constant.FLOOR_MAX;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);


                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == max) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) == min){
                                    for(int k = j; k < waitingLine.size(); ++k) {
                                        if(waitingLine.get(k) > targetFloor) {
                                            waitingLine.add(k, targetFloor);
                                            isAdded = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else if (targetFloor > currentFloor) {
                    System.out.println(">");
                    short max = Constant.FLOOR_MIN;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        max = max > waitingLine.get(i) ? max : waitingLine.get(i);

                    short min = Constant.FLOOR_MAX;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);


                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == max) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) == min){
                                    for(int k = j; k < waitingLine.size(); ++k) {
                                        if(waitingLine.get(k) < targetFloor) {
                                            System.out.println(waitingLine.get(k) + " : " + targetFloor);
                                            waitingLine.add(k, targetFloor);
                                            isAdded = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else {//targetFloor < currentFloor
                    System.out.println("<");
                    short max = Constant.FLOOR_MIN;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        max = max > waitingLine.get(i) ? max : waitingLine.get(i);

                    short min = Constant.FLOOR_MAX;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);


                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == max) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) == min){
                                    for(int k = j; k < waitingLine.size(); ++k) {
                                        if(waitingLine.get(k) < targetFloor) {
                                            waitingLine.add(k, targetFloor);
                                            isAdded = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
            }
            else if (directionAfterReachingTargetFloor.equals(Direction.DOWN)) {
                System.out.println("down : ");
                if (targetFloor == currentFloor) {
                    System.out.println("=");
                    short max = 0;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        max = max > waitingLine.get(i) ? max : waitingLine.get(i);

                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == max) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) < targetFloor) {
                                    waitingLine.add(j, targetFloor);
                                    isAdded = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else if (targetFloor > currentFloor) {
                    System.out.println(">");
                    short max = 0;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        max = max > waitingLine.get(i) ? max : waitingLine.get(i);

                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == max) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) < targetFloor) {
                                    waitingLine.add(j, targetFloor);
                                    isAdded = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else {//targetFloor < currentFloor.get()
                    System.out.println("<");
                    short max = 0;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        max = max > waitingLine.get(i) ? max : waitingLine.get(i);

                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == max) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) < targetFloor) {
                                    waitingLine.add(j, targetFloor);
                                    isAdded = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
            }
            else {
                System.out.println("?");
            }
        }
        else if(actualDirection.equals(Direction.DOWN)){
            System.out.println("down : ");
            if (directionAfterReachingTargetFloor.equals(Direction.STAY)) {
                System.out.println("stay : ");
                if (targetFloor == currentFloor) {
                    System.out.println("=");
                    short min = 0;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);

                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == min) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) > targetFloor) {
                                    waitingLine.add(j, targetFloor);
                                    isAdded = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else if (targetFloor > currentFloor) {
                    System.out.println(">");
                    short min = 0;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);

                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == min) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) > targetFloor) {
                                    waitingLine.add(j, targetFloor);
                                    isAdded = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else {//targetFloor < currentFloor.get()
                    System.out.println("<");
                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) < targetFloor) {
                            waitingLine.add(i, targetFloor);
                            isAdded = true;
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
            }
            else if (directionAfterReachingTargetFloor.equals(Direction.UP)) {
                System.out.println("up : ");
                if (targetFloor == currentFloor) {
                    System.out.println("=");
                    short min = 0;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);

                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == min) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) > targetFloor) {
                                    waitingLine.add(j, targetFloor);
                                    isAdded = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else if (targetFloor > currentFloor) {
                    System.out.println(">");
                    short min = 0;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);

                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == min) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) > targetFloor) {
                                    waitingLine.add(j, targetFloor);
                                    isAdded = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else {//targetFloor < currentFloor
                    System.out.println("<");
                    short min = 0;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);

                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == min) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) > targetFloor) {
                                    waitingLine.add(j, targetFloor);
                                    isAdded = true;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
            }
            else if (directionAfterReachingTargetFloor.equals(Direction.DOWN)) {
                System.out.println("down : ");
                if (targetFloor == currentFloor) {
                    System.out.println("=");
                    short max = Constant.FLOOR_MIN;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        max = max > waitingLine.get(i) ? max : waitingLine.get(i);

                    short min = Constant.FLOOR_MAX;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);


                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == min) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) == max){
                                    for(int k = j; k < waitingLine.size(); ++k) {
                                        if(waitingLine.get(k) > targetFloor) {
                                            waitingLine.add(k, targetFloor);
                                            isAdded = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else if (targetFloor > currentFloor) {
                    System.out.println(">");
                    short max = Constant.FLOOR_MIN;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        max = max > waitingLine.get(i) ? max : waitingLine.get(i);

                    short min = Constant.FLOOR_MAX;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);


                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == min) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) == max){
                                    for(int k = j; k < waitingLine.size(); ++k) {
                                        if(waitingLine.get(k) > targetFloor) {
                                            waitingLine.add(k, targetFloor);
                                            isAdded = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
                else {//targetFloor < currentFloor.get()
                    System.out.println("<");
                    short max = Constant.FLOOR_MIN;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        max = max > waitingLine.get(i) ? max : waitingLine.get(i);

                    short min = Constant.FLOOR_MAX;
                    for (int i = 0; i < waitingLine.size(); ++i)
                        min = min < waitingLine.get(i) ? min : waitingLine.get(i);


                    boolean isAdded = false;
                    for (int i = 0; i < waitingLine.size(); ++i){
                        if(waitingLine.get(i) == min) {
                            for(int j = i; j < waitingLine.size(); ++j){
                                if(waitingLine.get(j) == max){
                                    for(int k = j; k < waitingLine.size(); ++k) {
                                        if(waitingLine.get(k) < targetFloor) {
                                            waitingLine.add(k, targetFloor);
                                            isAdded = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (!isAdded) waitingLine.add(targetFloor);
                }
            }
            else {
                System.out.println("Bizarre");
            }
        }
        else { //si ascenseur en pose au moment de l'appui on re appel
            System.out.println("Bizarre");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            orderRequest(waitingLine, targetFloor, currentFloor, directionAfterReachingTargetFloor, actualDirection);
        }
    }
}