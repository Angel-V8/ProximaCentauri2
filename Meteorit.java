import processing.core.PApplet;
import processing.core.PVector;

public class Meteorit implements Entitat {

  // ATRIBUTS
  private PVector posicio;
  private PVector velocitat;
  private int tamany;
  private int dany;
  private int vida;

  // ATRIBUTS DE ROTACIÓ I SPRITES
  private float angle;
  private float rotationSpeed;
  private static processing.core.PImage spriteAsteroid;

  // ATRIBUTS DE DESTRUCCIÓ EN ANIMACIÓ
  private boolean destruint = false;
  private Animation animacioExplosio;

  // METODES
  public void actualitzar() {
    if (this.destruint) {
      if (this.animacioExplosio != null) {
        this.animacioExplosio.update();
      }
      // El meteorit frena a la quarta part de la seua velocitat mentre explota
      this.posicio.sub(PVector.mult(this.velocitat, 0.25f));
    } else {
      this.posicio.sub(this.velocitat);
      this.angle += this.rotationSpeed;
    }
  }

  public void mostrar(PApplet app) {
    if (this.destruint) {
      if (this.animacioExplosio == null) {
        // Spritesheet de 1024x1024 amb 4 files i 4 columnes -> 16 frames de 256x256
        this.animacioExplosio = new Animation(app, "Explosio", "./img/explosion.png", 256, 256, 4, 4, 0);
        this.animacioExplosio.setLoop(false);
        this.animacioExplosio.setDelay(2); // Animación rápida y fluida
      }
      // Dibuixem l'explosió amb un tamany proporcional (escala) al tamany del meteorit
      this.animacioExplosio.display(this.posicio, 1, (float)this.tamany / 128.0f);
    } else {
      if (spriteAsteroid == null) {
        spriteAsteroid = app.loadImage("./img/asteroid.png");
      }

      app.pushMatrix();
      app.translate(this.posicio.x, this.posicio.y);
      app.rotate(this.angle);
      app.imageMode(processing.core.PConstants.CENTER);

      if (spriteAsteroid != null) {
        app.image(spriteAsteroid, 0, 0, this.tamany, this.tamany);
      } else {
        // Fallback vectorial per seguretat
        app.fill(150);
        app.stroke(100);
        app.ellipse(0, 0, this.tamany, this.tamany);
      }

      app.popMatrix();
    }
  }

  public void rebreDany(int danyRebut) {
    if (this.destruint) return;
    this.vida -= danyRebut;
    if (this.vida <= 0) {
      this.destruint = true;
    }
  }

  public boolean estaDestruit() {
    return (this.vida <= 0);
  }

  // Mètodes auxiliars per a l'animació de destrucció no instantània
  public boolean isDestruint() {
    return this.destruint;
  }

  public boolean haAcabatExplosio() {
    return this.destruint && this.animacioExplosio != null && this.animacioExplosio.hasFinished();
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

    // Inicialització rotació aleatòria
    this.angle = (float)(Math.random() * Math.PI * 2);
    this.rotationSpeed = (float)(Math.random() * 0.06f) - 0.03f;
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

    // Els meteorits més xicotets roten més ràpid, els més grans roten més lentament
    this.rotationSpeed = ((float)(Math.random() * 0.08f) - 0.04f) * (40.0f / tamany);
  }

  public PVector getPosicio() {
    return this.posicio;
  }

  public int getTamany() {
    return this.tamany;
  }
}
