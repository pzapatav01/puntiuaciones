//Define el paquete al que pertenece la clase
package com.puntuaciones;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
 
public class Main extends Application {
 
    private static final String DB_URL = "jdbc:sqlite:highscores.db";
 
    public static void main(String[] args) {

        launch(args);

    }
 
    @Override

    public void start(Stage primaryStage) {

        // Crear la base de datos y la tabla si no existen

        createTable();
 
        // Crear los elementos de la interfaz

        Label nameLabel = new Label("Nombre del Jugador:");

        TextField nameField = new TextField();

        Label scoreLabel = new Label("Puntuación:");

        TextField scoreField = new TextField();

        Button submitButton = new Button("Actualizar Puntuación");

        TextArea highScoresArea = new TextArea();

        highScoresArea.setEditable(false);

        // Configurar el diseño de la ventana

        VBox layout = new VBox(10, nameLabel, nameField, scoreLabel, scoreField, submitButton, highScoresArea);

        layout.setPadding(new javafx.geometry.Insets(20));

        // Acción cuando se presiona el botón "Actualizar Puntuación"

        submitButton.setOnAction(event -> {

            String username = nameField.getText();

            int score = Integer.parseInt(scoreField.getText());

            updateHighScore(username, score);  // Actualizar puntuación

            showHighScores(highScoresArea);   // Mostrar los 10 mejores jugadores

        });

        // Crear la escena y configurarla

        Scene scene = new Scene(layout, 400, 400);

        primaryStage.setScene(scene);

        primaryStage.setTitle("Clasificación de Jugadores");

        primaryStage.show();

        // Mostrar los 10 mejores jugadores al iniciar

        showHighScores(highScoresArea);

    }
 
    // Crear la base de datos y la tabla si no existen

    public static void createTable() {

        String createTableSQL = "CREATE TABLE IF NOT EXISTS high_scores ("

                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "

                + "username TEXT NOT NULL UNIQUE, "

                + "score INTEGER NOT NULL"

                + ");";
 
        try (Connection conn = DriverManager.getConnection(DB_URL);

             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);

        } catch (SQLException e) {

            System.err.println(e.getMessage());

        }

    }
 
    // Actualizar la puntuación o agregar un nuevo jugador

    public static void updateHighScore(String username, int newScore) {

        String checkPlayerSQL = "SELECT score FROM high_scores WHERE username = ?";

        String insertSQL = "INSERT INTO high_scores (username, score) VALUES (?, ?)";

        String updateSQL = "UPDATE high_scores SET score = ? WHERE username = ?";

        String deleteSQL = "DELETE FROM high_scores WHERE id NOT IN ("

                + "SELECT id FROM high_scores ORDER BY score DESC LIMIT 10"

                + ");";
 
        try (Connection conn = DriverManager.getConnection(DB_URL);

             PreparedStatement checkStmt = conn.prepareStatement(checkPlayerSQL);

             PreparedStatement insertStmt = conn.prepareStatement(insertSQL);

             PreparedStatement updateStmt = conn.prepareStatement(updateSQL);

             Statement stmt = conn.createStatement()) {
 
            // Comprobar si el jugador ya existe

            checkStmt.setString(1, username);

            ResultSet rs = checkStmt.executeQuery();
 
            if (rs.next()) {

                int currentScore = rs.getInt("score");

                if (newScore > currentScore) {

                    // Si la nueva puntuación es mayor, actualizar la puntuación

                    updateStmt.setInt(1, newScore);

                    updateStmt.setString(2, username);

                    updateStmt.executeUpdate();

                }

            } else {

                // Si el jugador no existe, agregarlo a la tabla

                insertStmt.setString(1, username);

                insertStmt.setInt(2, newScore);

                insertStmt.executeUpdate();

            }
 
            // Limitar la tabla a los 10 mejores jugadores

            stmt.executeUpdate(deleteSQL);
 
            // Guardar los cambios

            conn.commit();
 
        } catch (SQLException e) {

            System.err.println(e.getMessage());

        }

    }
 
    // Mostrar los 10 mejores jugadores en el TextArea

    public static void showHighScores(TextArea highScoresArea) {

        String selectSQL = "SELECT username, score FROM high_scores ORDER BY score DESC LIMIT 10";

        StringBuilder highScoresText = new StringBuilder("Top 10 Jugadores:\n");
 
        try (Connection conn = DriverManager.getConnection(DB_URL);

             Statement stmt = conn.createStatement();

             ResultSet rs = stmt.executeQuery(selectSQL)) {
 
            while (rs.next()) {

                String username = rs.getString("username");

                int score = rs.getInt("score");

                highScoresText.append(username).append(": ").append(score).append("\n");

            }
 
            highScoresArea.setText(highScoresText.toString());
 
        } catch (SQLException e) {

            System.err.println(e.getMessage());

        }

    }

}

 