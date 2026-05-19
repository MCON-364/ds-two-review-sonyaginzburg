package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.Action;
import java.util.Optional;
import java.util.Stack;

/**
 * In-class Exercise 1 — Action History
 *
 * A simple editor needs to remember actions so the user can undo and redo work.
 *
 * Requirements:
 * - perform(action) records a newly completed action.
 * - undo() removes and returns the action that should be undone next.
 * - redo() removes and returns the action that should be redone next.
 * - undo() returns Optional.empty() when there is nothing available to undo.
 * - redo() returns Optional.empty() when there is nothing available to redo.
 * - performing a new action after one or more undo operations makes the old redo path invalid.
 * - getUndoCount() returns how many actions are currently available to undo.
 * - getRedoCount() returns how many actions are currently available to redo.
 *
 * You may add private fields and private helper methods.
 * Do not change the public method signatures.
 * Before coding, decide:
 * - What information does this class need to remember?
 * - What is the appropriate data structure
 * - Which operation should be fastest?
 * - When an action is undone, where should it go so it can be redone later?
 * - What should happen to redo history after a brand-new action is performed?

 */
public class ActionHistory {
    private final Stack<Action> undoStack = new Stack<>();
    private final Stack<Action> redoStack = new Stack<>();

    public void perform(Action action) {
        // TODO: implement based on the requirements above
        undoStack.push(action);
        redoStack.clear(); // clears the whole stack of redo

    }

    public Optional<Action> undo() {
        // TODO: implement based on the requirements above
        // TODO: if undoStack is empty, return Optional.empty()
        // TODO: otherwise move one action from undoStack to redoStack
        if (undoStack.isEmpty()) {
            return Optional.empty();
        }
        Action action = undoStack.pop();
        redoStack.push(action);
        return Optional.of(action);
    }

    public Optional<Action> redo() {
        // TODO: implement based on the requirements above
        if (redoStack.isEmpty()) {
            return Optional.empty();
        }
        // TODO: otherwise move one action from redoStack to undoStack
        Action action = redoStack.pop();
        undoStack.push(action);

        return Optional.of(action);
    }

    public int getUndoCount() {
        // implement based on the requirements above
        return undoStack.size();
    }

    public int getRedoCount() {
        //implement based on the requirements above
        return redoStack.size();
    }
}
