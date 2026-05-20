import processing.core.PVector;

public class Utils {

  // METODE ESTATIC PER A COLISIONS
  // Rep les posicions i tamanys de dos objectes i diu si es toquen
  public static boolean hiHaColisio(PVector pos1, int tam1, PVector pos2, int tam2) {
    
    // Calculem la distancia entre els dos punts
    float distancia = PVector.dist(pos1, pos2);
    
    // Calculem els radis
    float radi1 = tam1 / 2.0f;
    float radi2 = tam2 / 2.0f;
    
    // Si estan mes prop que la suma dels radis, s'estan tocant
    if (distancia < (radi1 + radi2)) {
      return true;
    } else {
      return false;
    }
  }
}
