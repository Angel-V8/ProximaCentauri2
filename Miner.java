import processing.core.PApplet;
import processing.core.PVector;

public class Miner extends Enemic {

  protected int temporizador;
  protected int cooldown;
  private PVector posicioJugador; // Referència a la posició del jugador

  public Miner(PVector posicioJugador) {
    super();
    this.vida = 60; // El miner té el doble de vida (6 tirs) per ser un enemic "pesat"
    this.velocitat = 1.5f;
    this.vel = this.direccio.copy().mult(velocitat);
    this.temporizador = 0;
    this.cooldown = 100; // Ara posa mines més ràpid (abans 150)
    this.posicioJugador = posicioJugador;
  }

  public void actualitzar() {
    super.actualitzar();
    
    // El miner es desplaça en vertical cap a l'altura (Y) del jugador per llançar mines en la seua línia
    if (this.posicioJugador != null) {
      float difY = this.posicioJugador.y - this.posicio.y;
      this.posicio.y += difY * 0.015f; // Desplaçament vertical lent i industrial
    }
    
    this.temporizador++;
  }

  public void mostrar(PApplet app) {
    app.fill(0, 255, 255);
    app.noStroke();
    app.ellipse(this.posicio.x, this.posicio.y, this.tamany, this.tamany);
    
    // NOU: Cridem al mètode del pare (Enemic) perquè dibuixe la barra de vida!
    super.mostrar(app);
  }

  public Mina deixarMina() {
    if (this.temporizador >= this.cooldown) {
      this.temporizador = 0;
      return new Mina(this.posicio);
    }
    return null;
  }
}
