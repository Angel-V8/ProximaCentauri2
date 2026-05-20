import processing.core.PApplet;
import processing.core.PVector;

public class MonstreFinal {

  // ATRIBUTS
  private PVector posicio;
  private int vida;
  private int ample;
  private int alt;
  private boolean actiu;

  // METODES
  public void actualitzar() {
    this.posicio.x = this.posicio.x - 1;
  }

  public void mostrar(PApplet app) {
    app.fill(100, 0, 0);
    app.rectMode(PApplet.CENTER);
    app.rect(this.posicio.x, this.posicio.y, this.ample, this.alt);
  }

  // CONSTRUCTORS

  // Constructor per defecte
  public MonstreFinal() {
    this.posicio = new PVector(700, 300); // Es queda a la dreta
    this.vida = 500;
    this.ample = 100;
    this.alt = 100;
    this.actiu = true;
  }

  // 2. Constructor parametritzat (Mes vida segons nivell)
  public MonstreFinal(int nivell) {
    this(); // Cridem al constructor per defecte per omplir la resta
    this.vida = 500 * nivell;
  }
}
