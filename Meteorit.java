import processing.core.PApplet;
import processing.core.PVector;

public class Meteorit implements Entitat {

  // ATRIBUTS
  private PVector posicio;
  private PVector velocitat;
  private int tamany;
  private int dany;
  private int vida;
  private int vidaMaxima; // NOU: Registrar la vida màxima per a la barra de vida

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
      // El meteorit frena a la quarta part de la seua velocitat mentre explota (in-place per evitar instanciar PVector)
      this.posicio.x -= this.velocitat.x * 0.25f;
      this.posicio.y -= this.velocitat.y * 0.25f;
    } else {
      this.posicio.sub(this.velocitat);
      this.angle += this.rotationSpeed;
    }
  }

  public void mostrar(PApplet app) {
    if (this.destruint) {
      if (this.animacioExplosio == null) {
        // Spritesheet de 256x256 amb 4 files i 4 columnes -> 16 frames de 64x64
        this.animacioExplosio = new Animation(app, "Explosio", "./img/explosion.png", 64, 64, 4, 4, 0);
        this.animacioExplosio.setLoop(false);
        this.animacioExplosio.setDelay(2); // Animación rápida y fluida
      }
      // Dibuixem l'explosió amb un tamany proporcional (escala) al tamany del meteorit
      this.animacioExplosio.display(this.posicio, 1, (float)this.tamany / 32.0f);
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

      // NOU: Dibuixem la barra de vida només si ha rebut dany (vida < vidaMaxima)
      if (this.vida < this.vidaMaxima) {
        app.pushStyle();
        app.rectMode(processing.core.PApplet.CORNER);
        app.fill(150, 0, 0); // Vermell fosc de fons
        app.rect(this.posicio.x - 15, this.posicio.y - (this.tamany/2 + 10), 30, 4);
        app.fill(0, 255, 0); // Verd de vida
        float ampleVida = PApplet.map(Math.max(0, this.vida), 0, this.vidaMaxima, 0, 30);
        app.rect(this.posicio.x - 15, this.posicio.y - (this.tamany/2 + 10), ampleVida, 4);
        app.popStyle();
      }
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
    this.vidaMaxima = 50; // NOU: Inicialitzar vida màxima per defecte

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
    this.vidaMaxima = tamany; // NOU: Inicialitzar vida màxima proporcional
    
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
