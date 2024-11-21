import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class JuliaParalelo extends JPanel {

    private static final int ANCHO = 1000;
    private static final int ALTO = 800;
    private static final int ITERACIONES_MAXIMAS = 10000;
    private static final double ZOOM_BASE = 300;


    private BufferedImage imagen;
    private double desplazamientoX = 0;
    private double desplazamientoY = 0;
    private double factorZoom = 1;

    public JuliaParalelo() {
        imagen = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        generarFractalParalelo();
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
                generarFractalParalelo();
                long tiempoFin = System.nanoTime();
                System.out.println("Tiempo de generación: " + (tiempoFin - tiempoInicio) / 1_000_000 + " ms");

                repaint();
            }
        });
    }
    // Versión paralela de generarFractal
    private void generarFractalParalelo() {
        double cX = -0.7269;
        double cY = 0.1889;

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        int filasPorHilo = ALTO / Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            int inicioY = i * filasPorHilo;
            int finY = (i + 1) * filasPorHilo;

            executor.submit(() -> {
                for (int x = 0; x < ANCHO; x++) {
                    for (int y = inicioY; y < finY; y++) {
                        double zx = (x - ANCHO / 2.0) / (ZOOM_BASE * factorZoom) + desplazamientoX;
                        double zy = (y - ALTO / 2.0) / (ZOOM_BASE * factorZoom) + desplazamientoY;
                        int iter = 0;

                        while (zx * zx + zy * zy < 4.0 && iter < ITERACIONES_MAXIMAS) {
                            double tmp = zx * zx - zy * zy + cX;
                            zy = 2.0 * zx * zy + cY;
                            zx = tmp;
                            iter++;
                        }

                        int color = iter < ITERACIONES_MAXIMAS ? Color.HSBtoRGB(iter / 256f, 1, iter / (iter + 8f)) : 0;
                        imagen.setRGB(x, y, color);
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imagen, 0, 0, null);
    }

    public static void main(String[] args) {
        JFrame ventana = new JFrame("Fractal Julia - Secuencial");
        JuliaParalelo panel=new JuliaParalelo();
        ventana.add(panel);
        ventana.setSize(ANCHO, ALTO);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setVisible(true);
    }

}
