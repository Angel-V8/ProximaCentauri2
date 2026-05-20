import processing.core.*; //<>//
import processing.data.*;
import java.util.ArrayList;
/************************************************************/
/*                                                          */
/*                Formatted Current Time                    */
/*                                                          */
/************************************************************/
/*                        score                             */
/************************************************************/
/*        lblPuntsA         *        lblPuntsB              */
/************************************************************/
/*          puntsA          *          puntsB               */
/************************************************************/
/*        lblExtra                Formatted CountDown       */
/************************************************************/
/**
 * La classe marcador és la classe que s'encarregara de portar el control del temps i mostrar-lo.
 * Aquesta classe disposarà de 2 comptadors A i B al quals se li assignarà una etiqueta: lblPuntsA o lblPuntsB
 * i un valor puntsA o puntsB que es motraràn a la pantalla principal de l'aplicació.
 * Els objectes de la classe marcador també disposaran d'un conjunt d'alarmes (Nom-Interaval) per que avise
 * a l'aplicació principal del pas de cert interval de temps, per exemple. Per tal d'implementar aquestes alarmes
 * utilitzarem un JSONArray encara que també podriem dissenyar la nostra classe Alarma amb tots els membres necessaris.
 */
public class Marcador {

  private PApplet app;                 /** Referència al PApplet principal */
  private int        currentTime;       /** Temps en milisegons actual (transcorregut des de la creació de l'objecte marcador) */
  private int        currentCountDown;  /** Temps de compte enrere en cas de vole utilitzar-se. Aquest atribut sol combinar-se amb countDownFlag */
  private int        countDownFlag;     /** Per controlar quan comencen i acaben els comptes enrere necessite un flag o inici del compte Enrere */
  private int        score;             /** Punts totals en cas d'haver-ne */
  private JSONArray  alarmes;           /** Col·lecció amb les alarmes establides al marcador */

  /**
   * Crea un marcador per defecte de 200 píxels d'ample, 90 d'alt els valors inicials són zero i a les etiquetes A i B posa HOME i VISIT
   * Per defecte aquest marcador es posiciona verticalment dalt de la pantalla i horitzontalment enmig
   * @param app referència al PApplet principal
   */
  public Marcador (PApplet app) {
    this.app = app;
    this.currentTime      = 0;
    this.currentCountDown = 0;
    this.score            = 0;
    this.alarmes          = new JSONArray();
  }

  /**
   * Mostra el HUD Avançat (Punts, Temps i Barra de Vida del jugador)
   */
  public void mostra (int vidaJugador) {
    app.pushStyle();

    // Fons del HUD (Banda fosca superior)
    app.fill(0, 0, 20, 180);
    app.noStroke();
    app.rect(0, 0, app.width, 50);

    // Puntuació
    app.fill(0, 255, 255); // Cian
    app.textSize(22);
    app.textAlign(PApplet.LEFT, PApplet.CENTER);
    app.text("SCORE: " + this.score, 20, 25);

    // Temps
    app.fill(255, 255, 255);
    app.textAlign(PApplet.RIGHT, PApplet.CENTER);
    app.text("TIME: " + this.getTime(), app.width - 20, 25);

    // HUD Central: Barra de Vida Jugador
    app.textAlign(PApplet.CENTER, PApplet.CENTER);
    app.fill(255);
    app.textSize(18);
    app.text("HP", app.width/2 - 130, 23);

    // Contorn de la barra
    app.stroke(255);
    app.strokeWeight(2);
    app.noFill();
    app.rect(app.width/2 - 100, 15, 200, 20, 5);

    // Color dinàmic de la barra (Verd -> Groc -> Roig)
    if (vidaJugador > 50) app.fill(0, 255, 0); // Verd
    else if (vidaJugador > 25) app.fill(255, 200, 0); // Groc/Taronja
    else app.fill(255, 0, 0); // Roig

    // Interior de la barra
    app.noStroke();
    float ampleVida = PApplet.map(Math.max(0, vidaJugador), 0, 100, 0, 196);
    app.rect(app.width/2 - 98, 17, ampleVida, 16, 3);

    app.popStyle();
  }

  /**
   * Actualitza els valors de temps del marcador
   */
  public void actualitza (int framesNivell) {
    // Per respectar l'arquitectura, no usem millis(). 
    // Convertim els frames a milisegons (a 60 FPS, 1 frame = ~16.6ms)
    this.currentTime = (int)((framesNivell / 60.0f) * 1000);

    if (this.currentCountDown > 0) {
      this.currentCountDown -= (this.currentTime - this.countDownFlag);
      this.countDownFlag = this.currentTime;
    }

    if (this.currentCountDown < 0)
      this.currentCountDown = 0;
  }

  /**
   * Aquest marcador disposa d'un sistema d'alarmes. En principi una alarma és un Objecte JSON amb
   * els següents camps: Tag, Inici, Durada, Repetir.
   * Tag: és l'etiqueta que servirà per identificar l'alarmaa i el seu tipus
   * Durada: temps en milisegons que ha de passar fins que salte l'alarma
   * Repetir: booleà que ens diu si l'alarma es repeteix periòdicament (true) o només una vegada (false)
   */
  public void afegirAlarma (String text, int durada, boolean esRepeteix) {
    JSONObject alarm = new JSONObject();
    alarm.setString  ("Tag", text);
    alarm.setInt     ("Inici", this.currentTime);
    alarm.setInt     ("Durada", durada);
    alarm.setBoolean ("Repetir", esRepeteix);
    this.alarmes.setJSONObject(this.alarmes.size(), alarm);
  }

  /**
   * Comprova l'array d'alarmes que s'han anat afegint al nostre marcador i
   * torna aquelles per a les quals ja s'ha complit el temps i han saltat.
   * Si l'alarma no és de repetició, quan salta s'elimina del conjunt d'alarmes del marcador.
   * @return llista amb les alarmes que s'han disparat des de l'última volta que s'ha comprovat.
   */
  public ArrayList<String> obtenirAlarmesDisparades () {
    ArrayList <String> alAlarmes = new ArrayList<String>();
    for (int i=0; i < this.alarmes.size(); i ++) {
      JSONObject alarm = this.alarmes.getJSONObject(i);
      String etiqueta = alarm.getString("Tag");
      int    inici    = alarm.getInt("Inici");
      int    durada   = alarm.getInt("Durada");

      if (this.currentTime >= inici + durada) {
        alarm.setInt("Inici", this.currentTime);
        alAlarmes.add(etiqueta);

        if (!alarm.getBoolean("Repetir"))
          this.alarmes.remove(i);
      }
    }
    return alAlarmes;
  }

  /******************************** GETS & SETS ********************************/
  public void setCountDown (int millis) {
    this.currentCountDown = millis;
    this.countDownFlag    = this.currentTime;
  }

  /**
   * Incrementa el marcador en 'inr' punts
   */
  public void incrementScore (int inr) {
    this.score += inr;
  }

  /**
   * Torna el temps en format MM:SS
   */
  public String getTime () {
    int seconds = (this.currentTime/1000)%60;
    int minutes = (this.currentTime/1000)/60;
    return app.nf(minutes, 2, 0) + ":" + app.nf(seconds, 2, 0);
  }

  public int getScore() {
    return this.score;
  }

  public void resetScore() {
    this.score = 0;
  }
}
