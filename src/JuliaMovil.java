import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.concurrent.*;

import static java.lang.Math.*;

public class JuliaMovil extends JPanel {
    private static final int ANCHO = 1000;
    private static final int ALTO = 800;
    private static final int ITERACIONES_MAXIMAS = 1000;
    private static final double ZOOM_BASE = 100;
    private static double angle = 0;

    private BufferedImage imagen;
    private double desplazamientoX = 0;
    private double desplazamientoY = 0;
    private double factorZoom = 1;

    public JuliaMovil() {
        imagen = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);

        // Generar el fractal inicial
        generarFractal();

        // Timer para animación :)
        Timer timer = new Timer(25, e -> {
            angle += 0.05;
            generarFractal();
            repaint();
        });
        timer.start();

        // Agregar controles para interacción manual
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> angle -= 0.1;
                    case KeyEvent.VK_RIGHT -> angle += 0.1;
                    case KeyEvent.VK_UP -> factorZoom *= 1.2;
                    case KeyEvent.VK_DOWN -> factorZoom /= 1.2;
                }
                generarFractalParalelo();
                repaint();
            }
        });
        setFocusable(true);
    }

    private void generarFractal() {
        double cX = sin(angle); // Valor dinámico de cX
        double cY = cos(angle); // Valor dinámico de cY

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
    }

    // Versión paralela de generarFractal
    private void generarFractalParalelo() {
        double cX = sin(angle);
        double cY = cos(angle);

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
        JFrame ventana = new JFrame("Fractal Julia - Animado");
        JuliaMovil panel = new JuliaMovil();
        ventana.add(panel);
        ventana.setSize(ANCHO, ALTO);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setVisible(true);
    }
}
