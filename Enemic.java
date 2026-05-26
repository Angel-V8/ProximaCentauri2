import processing.core.PApplet;
import processing.core.PVector;

public class Enemic implements Entitat {

  // ATRIBUTS
  protected PVector posicio;
  protected PVector direccio;
  protected PVector vel;
  protected int vida;
  protected int tamany;
  protected float velocitat;

  // METODES
  public void actualitzar() {
    this.posicio.sub(this.vel);
  }

  public void mostrar(PApplet app) {
    app.fill(255, 0, 0);
    app.noStroke();
    app.ellipse(this.posicio.x, this.posicio.y, this.tamany, this.tamany);

    dibuixarBarraVida(app, 30);
  }

  protected void dibuixarBarraVida(PApplet app, int vidaMaxima) {
    app.pushStyle();
    app.rectMode(PApplet.CORNER);
    app.fill(150, 0, 0);
    app.rect(this.posicio.x - 15, this.posicio.y - 25, 30, 4);
    app.fill(0, 255, 0);
    float ampleVida = PApplet.map(Math.min(vidaMaxima, Math.max(0, this.vida)), 0, vidaMaxima, 0, 30);
    app.rect(this.posicio.x - 15, this.posicio.y - 25, ampleVida, 4);
    app.popStyle();
  }

  public void rebreDany(int dany) {
    this.vida -= dany;
    if (this.vida < 0) this.vida = 0;
  }

  public boolean estaDestruit() {
    return (this.vida <= 0);
  }

  public PVector getPosicio() {
    return this.posicio;
  }

  public int getTamany() {
    return this.tamany;
  }

  // CONSTRUCTORS
  public Enemic() {
    this.vida = 30; // Ara necessiten 3 tirs per morir (cada tir de jugador fa 10 de dany)
    this.tamany = 30;
    
    // Apareix fora de la pantalla per la dreta
    float y = (float)(Math.random() * 500) + 50;
    this.posicio = new PVector(850, y);
    this.direccio = new PVector(1, 0); // Va cap a l'esquerra
    this.velocitat = 4;
    this.vel = this.direccio.copy().mult(velocitat);
  }
}
