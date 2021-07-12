
package ch.codez.autophoto.controller;

public interface DirectorListener {
    
    public void countDownAt(int i);
    
    public void processing();
    
    public void ready(String filename);
    
}
