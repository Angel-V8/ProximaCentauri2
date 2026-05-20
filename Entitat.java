import processing.core.PApplet;

public interface Entitat {
    void actualitzar();
    void mostrar(PApplet app);
    
    void rebreDany(int dany);
    boolean estaDestruit();
}
