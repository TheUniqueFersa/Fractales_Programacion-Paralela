import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JuliaSecuencial extends JPanel{
    private static final int ANCHO = 1000;
    private static final int ALTO = 800;
    private static final int ITERACIONES_MAXIMAS = 10000;
    private static final double ZOOM_BASE = 300;


    private BufferedImage imagen;
    private double desplazamientoX = 0;
    private double desplazamientoY = 0;
    private double factorZoom = 1;

    public JuliaSecuencial() {
        imagen = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        generarFractal();
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evento) {
                int x = evento.getX();
                int y = evento.getY();


                // Calcula el nuevo centro en el plano complejo
                desplazamientoX += (x - ANCHO / 2.0) / (ZOOM_BASE * factorZoom);
                desplazamientoY += (y - ALTO / 2.0) / (ZOOM_BASE * factorZoom);

                // Ajusta el zoom
                if (evento.getButton() == java.awt.event.MouseEvent.BUTTON1) { // Zoom in
                    factorZoom *= 1.5;
                } else if (evento.getButton() == java.awt.event.MouseEvent.BUTTON3) { // Zoom out
                    factorZoom /= 1.5;
                }

                // Mide el tiempo de generación
                long tiempoInicio = System.nanoTime();
                generarFractal();
                long tiempoFin = System.nanoTime();
                System.out.println("Tiempo de generación: " + (tiempoFin - tiempoInicio) / 1_000_000 + " ms");

                repaint();
            }
        });
    }

    private void generarFractal() {
        double cX = -0.7269;
        double cY = 0.1889;


        for (int x = 0; x < ANCHO; x++) {
            for (int y = 0; y < ALTO; y++) {
                double zx = (x - ANCHO / 2.0) / (ZOOM_BASE * factorZoom) + desplazamientoX;
                double zy = (y - ALTO / 2.0) / (ZOOM_BASE * factorZoom) + desplazamientoY;
                int iter = 0;

                while (zx * zx + zy * zy < 4.0 && iter < ITERACIONES_MAXIMAS) {
                    double tmp = zx * zx - zy * zy + cX;
                    zy = 2.0 * zx * zy + cY;
                    zx = tmp;
                    iter++;
                }

                // Asignar color basado en las iteraciones
                int color = iter < ITERACIONES_MAXIMAS ? Color.HSBtoRGB(iter / 256f, 1, iter / (iter + 8f)) : 0;
                imagen.setRGB(x, y, color);
            }
        }
        System.out.println("Fractal de Julia generado.");
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imagen, 0, 0, null);
    }

    public static void main(String[] args) {
        JFrame ventana = new JFrame("Fractal Julia - Secuencial");
        JuliaSecuencial panel=new JuliaSecuencial();
        ventana.add(panel);
        ventana.setSize(ANCHO, ALTO);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setVisible(true);
    }
}