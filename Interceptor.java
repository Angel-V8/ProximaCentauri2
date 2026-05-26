import processing.core.PApplet;
import processing.core.PVector;

public class Interceptor extends Enemic {
    
  private float yInicial;
  private float angle;
  
  protected int temporizador;
  protected int cooldown;
  private PVector posicioJugador; // Referència a la posició del jugador
  private Animation animacio; // Animació dels propulsors de l'Interceptor

  public Interceptor(PVector posicioJugador) {
    super();
    this.velocitat = 2.5f;
    this.vel = this.direccio.copy().mult(velocitat);
    this.temporizador = 0;
    this.cooldown = 40; 
    
    this.yInicial = this.posicio.y; 
    this.angle = 0;
    this.posicioJugador = posicioJugador;
  }

  public void actualitzar() {
    super.actualitzar();
    
    if (!this.isDestruint()) {
      // Moviment ondulat (sinusoide)
      this.angle += 0.03f; 
      this.posicio.y = this.yInicial + (float)Math.sin(this.angle) * 30; 
      
      this.temporizador++;

      if (this.animacio != null) {
        this.animacio.update();
      }
    }
  }

  public void mostrar(PApplet app) {
    if (this.isDestruint()) {
      super.mostrar(app); // Dibuixa l'animació d'explosió heredada
      return;
    }

    if (this.animacio == null) {
      // Spritesheet de 1024x1024 en quadrícula 2x2 -> 4 frames de 512x512
      this.animacio = new Animation(app, "Interceptor", "./img/interceptor.png", 512, 512, 2, 2, 0);
      this.animacio.setLoop(true);
      this.animacio.setDelay(6); // Animación fluida de propulsores magenta
    }

    this.animacio.display(this.posicio, 1, (float)this.tamany / 512.0f);
    
    // Dibuixem la barra de vida de 30 HP màxims
    super.dibuixarBarraVida(app, 30);
  }

  public Dispar disparar() {
    if (this.isDestruint()) return null; // No dispara si està explotant
    if (this.temporizador >= this.cooldown) {
      this.temporizador = 0;
      if (this.posicioJugador != null) {
        return new Dispar(this.posicio, this.posicioJugador, true); 
      }
      return new Dispar(this.posicio, true); 
    }
    return null;
  }
}
