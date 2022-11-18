package com.kalle.gogame;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 消息弹窗
 *
 * @author xiexincong
 * @since 2022/11/18
 */
public class Toast {

    private static final Stage stage = new Stage();

    static {
        // 背景透明
        stage.initStyle(StageStyle.TRANSPARENT);
    }

    // 默认3秒
    public static void toast(String msg) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(stage::close);
            }
        };
        init(msg);
        stage.show();
        Timer timer = new Timer();
        timer.schedule(task, 3000);
    }

    // 设置消息
    private static void init(String msg) {
        // 默认信息
        Label label = new Label(msg);
        // label透明,圆角
        label.setStyle("-fx-background: rgba(56,56,56,0.7);-fx-border-radius: 25;-fx-background-radius: 25");
        // 消息字体颜色
        label.setTextFill(Color.rgb(225, 255, 226));
        label.setPrefHeight(30);
        label.setPadding(new Insets(10));
        // 居中
        label.setAlignment(Pos.CENTER);
        // 字体大小
        label.setFont(new Font(14));
        Scene scene = new Scene(label);
        // 场景透明
        scene.setFill(null);
        stage.setScene(scene);
    }
}
