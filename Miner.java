import processing.core.PApplet;
import processing.core.PVector;

public class Miner extends Enemic {

  protected int temporizador;
  protected int cooldown;
  private PVector posicioJugador; // Referència a la posició del jugador
  private Animation animacio; // Animació dels propulsors del Miner

  public Miner(PVector posicioJugador) {
    super();
    this.vida = 60; // El miner té el doble de vida (6 tirs) per ser un enemic "pesat"
    this.velocitat = 1.5f;
    this.vel = this.direccio.copy().mult(velocitat);
    this.temporizador = 0;
    this.cooldown = 100; // Ara posa mines més ràpid (abans 150)
    this.posicioJugador = posicioJugador;
    this.tamany = 50; // NOU: Increment de tamany a 50 px per a reflectir que és un enemic pesat
  }

  public void actualitzar() {
    super.actualitzar();
    
    if (!this.isDestruint()) {
      // El miner es desplaça en vertical cap a l'altura (Y) del jugador per llançar mines en la seua línia
      if (this.posicioJugador != null) {
        float difY = this.posicioJugador.y - this.posicio.y;
        this.posicio.y += difY * 0.015f; // Desplaçament vertical lent i industrial
      }
      
      this.temporizador++;

      if (this.animacio != null) {
        this.animacio.update();
      }
    }
  }

  public void mostrar(PApplet app) {
    if (this.isDestruint()) {
      super.mostrar(app); // NOU: Dibuixa l'animació d'explosió si s'està destruint
      return;
    }

    if (this.animacio == null) {
      // Spritesheet de 1024x1024 en quadrícula 2x2 -> 4 frames de 512x512
      this.animacio = new Animation(app, "Miner", "./img/miner.png", 512, 512, 2, 2, 0);
      this.animacio.setLoop(true);
      this.animacio.setDelay(6); // Velocitat d'animació fluida per als propulsors
    }

    this.animacio.display(this.posicio, 1, (float)this.tamany / 512.0f);
    
    // Dibuixem la barra de vida de 60 HP màxims (el miner té 60 HP)
    super.dibuixarBarraVida(app, 60);
  }

  public Mina deixarMina() {
    if (this.isDestruint()) return null; // NOU: No solta mines si s'està destruint
    if (this.temporizador >= this.cooldown) {
      this.temporizador = 0;
      return new Mina(this.posicio);
    }
    return null;
  }
}
