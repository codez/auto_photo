package ch.codez.autophoto.controller;

public interface WorkerListener {
    public void workerStarted();

    public void workerStopped();

    public void workerImageProcessed();
}
