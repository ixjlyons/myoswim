package ixjlyons.myosky.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;

public class Coin extends Actor {
    
    private TextureRegion textureRegion;
    private Circle bound;
    private boolean counted = false;
    
    public Coin() {
        this(0, 0);
    }
    
    public Coin(float x, float y) {
        setPosition(x, y);
        
        textureRegion = new TextureRegion(new Texture("coin.png"));
        setBounds(
                getX(), getY(),
                textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
        bound = new Circle(getX(), getY(), getWidth()/2);
    }
    
    public void setCounted(boolean counted) {
        this.counted = counted;
    }
    
    public boolean getCounted() {
        return counted;
    }
    
    @Override
    public void act(float delta) {
        super.act(delta);
        bound.set(getX(Align.center), getY(Align.center), getWidth()/2);
    }
        
    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        batch.draw(
                textureRegion,
                getX(), getY(),
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                getScaleX(), getScaleY(),
                getRotation());
    }
    
    public Circle getCircle() {
        return bound;
    }
}