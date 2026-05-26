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
  protected boolean destruint = false;
  protected Animation animacioExplosio;

  // METODES
  public void actualitzar() {
    if (this.destruint) {
      if (this.animacioExplosio != null) {
        this.animacioExplosio.update();
      }
      // Frena a la quarta part de la seua velocitat
      this.posicio.sub(PVector.mult(this.vel, 0.25f));
    } else {
      this.posicio.sub(this.vel);
    }
  }

  public void mostrar(PApplet app) {
    if (this.destruint) {
      if (this.animacioExplosio == null) {
        this.animacioExplosio = new Animation(app, "Explosio", "./img/explosion.png", 256, 256, 4, 4, 0);
        this.animacioExplosio.setLoop(false);
        this.animacioExplosio.setDelay(2);
      }
      this.animacioExplosio.display(this.posicio, 1, (float)this.tamany / 128.0f);
    } else {
      app.fill(255, 0, 0);
      app.noStroke();
      app.ellipse(this.posicio.x, this.posicio.y, this.tamany, this.tamany);

      dibuixarBarraVida(app, 30);
    }
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
    if (this.destruint) return;
    this.vida -= dany;
    if (this.vida <= 0) {
      this.vida = 0;
      this.destruint = true;
    }
  }

  public boolean estaDestruit() {
    return (this.vida <= 0);
  }

  public boolean isDestruint() {
    return this.destruint;
  }

  public boolean haAcabatExplosio() {
    return this.destruint && this.animacioExplosio != null && this.animacioExplosio.hasFinished();
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
