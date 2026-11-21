package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;
import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

import java.util.ArrayList;
import java.util.List;

// dynamically resizing byte array
public class DynamicByteArray implements HasOutput {
  private int size = 128;
  private byte[] array = new byte[size];
  private int pointer = 0;

  // Positions (within this buffer) of the 2-byte offset field of a GOTO instruction emitted for a
  // "break"/"continue" statement whose target isn't known yet. Populated by BreakGenerator and
  // ContinueGenerator; resolved (patched) by the enclosing loop/switch generator once the jump
  // target is known. When a nested buffer (e.g. an if-block's body) is appended into a parent
  // buffer before that happens, its pending positions must be re-based with mergePending() so the
  // enclosing loop generator can still find and patch them.
  public List<Integer> pendingBreaks = new ArrayList<>();
  public List<Integer> pendingContinues = new ArrayList<>();
  // positions of labeled breaks/continues ("break outer;"), by label: they may target a loop
  // further out than the innermost one
  public java.util.Map<String, List<Integer>> labeledBreaks = new java.util.HashMap<>();
  public java.util.Map<String, List<Integer>> labeledContinues = new java.util.HashMap<>();

  // The breaks a loop with the given label (null if unlabeled) must patch: the unlabeled ones and
  // those targeting its label (which are removed from the labeled ones).
  public List<Integer> breaksFor(String label) {
    List<Integer> result = new ArrayList<>(pendingBreaks);
    List<Integer> labeled = (label != null) ? labeledBreaks.remove(label) : null;
    if (labeled != null) {
      result.addAll(labeled);
    }
    return result;
  }

  // The continues a loop with the given label (null if unlabeled) must patch, see breaksFor()
  public List<Integer> continuesFor(String label) {
    List<Integer> result = new ArrayList<>(pendingContinues);
    List<Integer> labeled = (label != null) ? labeledContinues.remove(label) : null;
    if (labeled != null) {
      result.addAll(labeled);
    }
    return result;
  }

  // Takes over the child's labeled breaks/continues targeting an outer loop; the child's bytes
  // are written to this buffer starting at position base.
  public void mergeLabeled(DynamicByteArray child, int base) {
    for (java.util.Map.Entry<String, List<Integer>> entry : child.labeledBreaks.entrySet()) {
      for (int pos : entry.getValue()) {
        labeledBreaks.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(pos + base);
      }
    }
    for (java.util.Map.Entry<String, List<Integer>> entry : child.labeledContinues.entrySet()) {
      for (int pos : entry.getValue()) {
        labeledContinues.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(pos + base);
      }
    }
  }

  // Imports another buffer's unresolved break/continue positions, shifting them by this buffer's
  // current length. Call this right before appending that buffer's bytes with write(byte[]).
  public void mergePending(DynamicByteArray child) {
    int base = size();
    for (int pos : child.pendingBreaks) {
      pendingBreaks.add(pos + base);
    }
    for (int pos : child.pendingContinues) {
      pendingContinues.add(pos + base);
    }
    mergeLabeled(child, base);
  }

  @Override
  public void write(byte b) {
    if (pointer >= size) {
      size *= 2;
      byte[] help = new byte[size];
      for (int i = 0; i < size / 2; i++) {
        help[i] = array[i];
      }
      array = help;
    }
    array[pointer] = b;
    pointer++;
  }

  @Override
  public void write(byte[] b) {
    while (pointer + b.length > size) {
      size *= 2;
      byte[] help = new byte[size];
      for (int i = 0; i < size / 2; i++) {
        help[i] = array[i];
      }
      array = help;
    }
    for (int i = 0; i < b.length; i++) {
      array[pointer] = b[i];
      pointer++;
    }
  }

  public int size() {
    return pointer;
  }

  public byte[] flush() {
    byte[] retValue = getBytes();
    size = 2;
    pointer = 0;
    array = new byte[size];
    return retValue;
  }

  @Override
  public byte[] getBytes() {
    byte[] retValue = new byte[pointer];
    for (int i = 0; i < pointer; i++) {
      retValue[i] = array[i];
    }
    return retValue;
  }

  @Override
  public void write(short val) {
    write(shortToByteArray(val));
  }

  @Override
  public void write(int val) {
    write(intToByteArray(val));
  }

  public void patch(int pos, byte[] bytes) {
    for (int i = 0; i < bytes.length; i++) {
      array[pos + i] = bytes[i];
    }
  }
}
