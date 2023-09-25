package de.antonpieper.glmaker.opengl;

public class TextureBinder {
    private int nextId = 0;

    public int bind() {
        return nextId++;
    }
}
