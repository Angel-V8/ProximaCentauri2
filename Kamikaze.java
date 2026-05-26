import processing.core.PApplet;
import processing.core.PVector;

public class Kamikaze extends Enemic {

  private PVector posicioJugador; // Referència a la posició del jugador per seguir-lo en temps real

  public Kamikaze(PVector posicioJugador) {
    super(); // Açò el fa nàixer en X=850 i Y=aleatòria
    
    this.vida = 20; // El kamikaze és més fràgil, mor de 2 tirs
    this.velocitat = 7; // Ràpid, però esquivable
    this.posicioJugador = posicioJugador; // Guardem la referència per al seguiment dinàmic
    
    // Calculem la direcció inicial cap al jugador
    PVector objectiu = posicioJugador.copy();
    this.direccio = objectiu.sub(this.posicio);
    this.direccio.normalize();
    
    this.vel = this.direccio.copy().mult(this.velocitat);
  }
  
  // Sobreescrivim com es mou per fer que seguisca la nau del jugador de forma suau
  public void actualitzar() {
    if (this.isDestruint()) {
      if (this.animacioExplosio != null) {
        this.animacioExplosio.update();
      }
      // Deriva cap endavant a la quarta part de la seua velocitat
      this.posicio.add(PVector.mult(this.vel, 0.25f));
      return;
    }

    // Si encara està a la dreta del jugador, corregim la direcció de forma gradual
    if (this.posicioJugador != null && this.posicio.x > this.posicioJugador.x) {
      PVector objectiu = this.posicioJugador.copy();
      PVector direccioDesitjada = objectiu.sub(this.posicio);
      direccioDesitjada.normalize();
      
      // Interpolació suau (lerp) per a un gir progressiu i dinàmic
      this.direccio.lerp(direccioDesitjada, 0.05f);
      this.direccio.normalize();
      
      // Actualitzem la velocitat en base a la nova direcció
      this.vel = this.direccio.copy().mult(this.velocitat);
    }
    
    // Apliquem el moviment
    this.posicio.add(this.vel);
  }

  public void mostrar(PApplet app) {
    if (this.isDestruint()) {
      super.mostrar(app); // Dibuixa l'animació d'explosió heredada de Enemic
      return;
    }

    app.fill(255, 255, 0); // Groc
    app.noStroke();
    app.ellipse(this.posicio.x, this.posicio.y, this.tamany, this.tamany);

    // Barra de vida
    app.pushStyle();
    app.rectMode(PApplet.CORNER);
    app.fill(150, 0, 0);
    app.rect(this.posicio.x - 15, this.posicio.y - 25, 30, 4);
    app.fill(0, 255, 0);
    float ampleVida = PApplet.map(Math.max(0, this.vida), 0, 20, 0, 30);
    app.rect(this.posicio.x - 15, this.posicio.y - 25, ampleVida, 4);
    app.popStyle();
  }
}
