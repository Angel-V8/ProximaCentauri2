import processing.core.PApplet;
import processing.core.PVector;

public class Meteorit implements Entitat {

  // ATRIBUTS
  private PVector posicio;
  private PVector velocitat;
  private int tamany;
  private int dany;
  private int vida;

  // METODES
  public void actualitzar() {
    this.posicio.sub(this.velocitat);
  }

  public void mostrar(PApplet app) {
    app.fill(150);
    app.stroke(100);
    app.ellipse(this.posicio.x, this.posicio.y, this.tamany, this.tamany);
  }

  public void rebreDany(int danyRebut) {
    this.vida -= danyRebut;
  }

  public boolean estaDestruit() {
    return (this.vida <= 0);
  }

  // CONSTRUCTORS

  // Constructor per defecte
  public Meteorit() {
    float y = (float)(Math.random() * 500) + 50;
    this.posicio = new PVector(900, y);
    
    // NOU: Velocitat vertical aleatòria per a trajectòries diagonals interessants
    float vy = (float)(Math.random() * 1.6f) - 0.8f;
    this.velocitat = new PVector(4, vy);
    this.tamany = 40;
    this.dany = 10;
    this.vida = 50;
  }

  // Constructor parametritzat
  public Meteorit(int tamany, int dany) {
    this(); // Cridem al constructor per defecte per omplir la resta
    this.tamany = tamany;
    this.dany = dany;
    
    // NOU: La vida és proporcional a la mida (1 tir per cada 10 de diàmetre)
    this.vida = tamany; 
    
    // NOU: Velocitat vertical proporcional a la mida i direcció aleatòria
    float vy = (float)(Math.random() * 2.0f) - 1.0f;
    // Els meteorits grans són més lents, els menuts més ràpids
    this.velocitat = new PVector(1.0f + (120.0f / tamany), vy);
  }

  public PVector getPosicio() {
    return this.posicio;
  }

  public int getTamany() {
    return this.tamany;
  }
}
