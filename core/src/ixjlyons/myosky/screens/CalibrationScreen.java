package ixjlyons.myosky.screens;

import ixjlyons.myosky.PlaneGame;
import ixjlyons.myosky.Processor;
import ixjlyons.myosky.RecordThread.OnReadListener;
import ixjlyons.myosky.actors.SignalViewer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class CalibrationScreen implements Screen, OnReadListener {
    
    public static final String TEXT = 
            "This is the smoothed, root mean square (RMS) signal." +
            "\n" +
            "Touch the screen to set the calibration level.";
    final PlaneGame game;

    private Skin skin;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    
    private float[] inputData = new float[100];
    private Processor processor;
    private float thresh = -1;
    
    private Stage stage;
    private Image background;
    private Button nextButton;
    private Button prevButton;
    private SignalViewer signalViewer;
    private Vector3 touchPoint;
    
    public CalibrationScreen(final PlaneGame game) {
        this.game = game;

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        
        font = new BitmapFont(Gdx.files.internal("arial.fnt"));
        font.setColor(0.3f, 0.3f, 0.3f, 1);
        glyphLayout = new GlyphLayout();
        glyphLayout.setText(font, TEXT);
        
        camera = new OrthographicCamera();
        camera.setToOrtho(false, PlaneGame.WIDTH, PlaneGame.HEIGHT);
        
        shapeRenderer = new ShapeRenderer();
        
        stage = new Stage(new ExtendViewport(PlaneGame.WIDTH, PlaneGame.HEIGHT));
        initBackground();
        initButtons();
        stage.addActor(background);
        stage.addActor(nextButton);
        stage.addActor(prevButton);
        
        signalViewer = new SignalViewer(0, stage.getHeight(), stage.getWidth(), 0);
        
        processor = new Processor();
        touchPoint = new Vector3();
    }
    
    private void initBackground() {
        background = new Image(new Texture("background.png"));
        background.setPosition(0, 0);
        background.setSize(stage.getWidth(), stage.getHeight());
    }
    
    private void initButtons() {
        float buttonWidth = 60f;
        float buttonHeight = 50f;
        float padding = 20f;
        
        prevButton = new TextButton("<", skin, "default");
        prevButton.setWidth(buttonWidth);
        prevButton.setHeight(buttonHeight);
        prevButton.setPosition(
               stage.getWidth()-2*buttonWidth-2*padding,
               padding);
        prevButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.prevScreen();
            }
        });
        
        nextButton = new TextButton(">", skin, "default");
        nextButton.setWidth(buttonWidth);
        nextButton.setHeight(buttonHeight);
        nextButton.setPosition(
                stage.getWidth()-buttonWidth-padding,
                padding);
        nextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.nextScreen();
            }
        });
    }
    
    @Override
    public void onRead(float[] data) {
        final float input = processor.update(data);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                updateDataBuffer(input);
            }
        });
    }
    
    private void updateDataBuffer(float input) {
        for (int i = 0; i < inputData.length-1; i++) {
            inputData[i] = inputData[i+1];
        }
        inputData[inputData.length-1] = input;
        
        signalViewer.setData(inputData);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glLineWidth(3);

        if (Gdx.input.isTouched()) {
            touchPoint.set(0, Gdx.input.getY(), 0);
            thresh = signalViewer.setThresh(camera.unproject(touchPoint).y);
        }
        
        stage.act();
        stage.draw();
        
        camera.update();
        
        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();
        font.draw(
                game.getSpriteBatch(),
                TEXT,
                PlaneGame.WIDTH/2f - glyphLayout.width/2,
                3*stage.getHeight()/4 + glyphLayout.height/2,
                glyphLayout.width,
                Align.center,
                false);
        game.getSpriteBatch().end();
        
        if (inputData == null) {
            return;
        }
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Line);
        signalViewer.draw(shapeRenderer);

        shapeRenderer.end();
    }
    
    public float getThreshold() {
        return (thresh - stage.getHeight()/2) / stage.getHeight();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        game.recordThread.setOnReadListener(this);
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
        stage.dispose();
    }
}