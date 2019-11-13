package com.jade.scripts;

import com.jade.components.Component;
import com.jade.components.SubSprite;
import com.jade.main.Window;

import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class Button extends Component {

    public int width, height;
    public SubSprite img, imgSelected;
    public String text;

    private boolean isSelected = false;
    private int framesForAnimation = 3;
    private int framesLeft = 0;
    private int wrapLength, fontSize;
    private int xBuffer, yBuffer;

    private float debounceTime = 0.1f;
    private float debounceTimeLeft = 0.0f;

    public Button(int width, int height, SubSprite img, SubSprite imgSelected, String text) {
        this.width = width;
        this.height = height;
        this.img = img;
        this.imgSelected = imgSelected;
        this.text = text;

        this.fontSize = 18;
        this.wrapLength = 70;
        this.xBuffer = (int)( (width - wrapLength) / 2.0);
        this.yBuffer = (int)( (height - fontSize) / 2.0);
    }

    public abstract void buttonPressed();

    @Override
    public void update(double dt) {
        if (debounceTimeLeft > 0) debounceTimeLeft -= dt;

        if (Window.mouseListener.mousePressed && Window.mouseListener.mouseButton == MouseEvent.BUTTON1 && debounceTimeLeft <= 0.0f) {
            debounceTimeLeft = debounceTime;
            if (Window.mouseListener.x >= this.parent.transform.position.x && Window.mouseListener.x <= this.parent.transform.position.x + width &&
                    Window.mouseListener.y >= this.parent.transform.position.y && Window.mouseListener.y <= this.parent.transform.position.y + height) {
                this.buttonPressed();
                isSelected = true;
                framesLeft = framesForAnimation;
            }
        }

        if (isSelected && framesLeft > 0) {
            framesLeft--;
        } else if (isSelected && framesLeft == 0) {
            isSelected = false;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Pusab", Font.PLAIN, fontSize));
        FontMetrics fm = g2.getFontMetrics();

        if (!isSelected) {
            g2.drawImage(img.subImg.image, (int)this.parent.transform.position.x, (int)this.parent.transform.position.y, width, height, null);
        } else {
            g2.drawImage(imgSelected.subImg.image, (int)this.parent.transform.position.x, (int)this.parent.transform.position.y, width, height, null);
        }

        drawTextWrapped(fm, g2);
    }

    private void drawTextWrapped(FontMetrics fm, Graphics2D g2) {
        int lineHeight = fm.getHeight();

        int startX = (int)this.parent.transform.position.x + xBuffer;
        int currentX = startX;
        int currentY = (int)this.parent.transform.position.y + yBuffer;

        for (char c : text.toCharArray()) {
            if (c == '\n') {
                currentX = startX;
                currentY += lineHeight;
                continue;
            }
            g2.drawString("" + c, currentX, currentY);

            currentX += fm.stringWidth("" + c);
            if (currentX >= startX + wrapLength) {
                currentX = startX;
                currentY += lineHeight;
            }
        }
    }
}
