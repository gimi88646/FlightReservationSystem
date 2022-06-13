package Validations;


public class FiniteAutomata {
    public int numberOfStates;
    public char[] allowedChars;
    public int initialState;
    public int[] finalStates;
    public int[][] transitionTable;

    public FiniteAutomata(char[] inputChars, int initialState, int[] finalStates, int[][] transitionTable){
        this.numberOfStates=transitionTable.length;
        this.allowedChars = inputChars;
        this.initialState = initialState;
        this.finalStates = finalStates;
        this.transitionTable= transitionTable;
    }

    public boolean validate(String word){
        int currentState = initialState;
        for(char ch :word.toCharArray()){
            boolean exists = false;
            //check if the character exist in allowedChars
            int i =0;
            for(;i<allowedChars.length;i++){
                if (ch==allowedChars[i]){
                    exists = true;
                    break;
                }
            }
            if(!exists) return false;
            // if word exits perform transition , "currentState" index points which state we are at, whereas "i" be the new state
            currentState = transitionTable[currentState][i];
        }
        //check if the current state is in set of final states
        for(int finalState: finalStates) {
            if(currentState ==finalState){
                return true;
            }
        }
        return false;
    }
}


