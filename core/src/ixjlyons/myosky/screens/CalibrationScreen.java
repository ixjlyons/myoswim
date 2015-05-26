package ixjlyons.myosky.screens;

import ixjlyons.myosky.PlaneGame;
import ixjlyons.myosky.Processor;
import ixjlyons.myosky.RecordThread.OnReadListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class CalibrationScreen implements Screen, OnReadListener {
    
    private static final float[] LINE_COLOR = {
        0x50 / 256f,
        0xce / 256f,
        0xa2 / 256f,
        1f
    };
    
    final PlaneGame game;
    
    private SpriteBatch batch;
    private Skin skin;
    private BitmapFont font;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    
    private float[] inputData = new float[100];
    private Processor processor;
    private int thresh = -1;
    
    private Stage stage;
    private Image background;
    private Button nextButton;
    
    public CalibrationScreen(final PlaneGame game) {
        this.game = game;
        
        batch = new SpriteBatch();
        font = new BitmapFont();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        
        shapeRenderer = new ShapeRenderer();
        
        stage = new Stage(new ExtendViewport(800, 480));
        initBackground();
        initButtons();
        stage.addActor(background);
        stage.addActor(nextButton);
        
        processor = new Processor();
    }
    
    private void initBackground() {
        background = new Image(new Texture("background.png"));
        background.setPosition(0, 0);
        background.setSize(stage.getWidth(), stage.getHeight());
    }
    
    private void initButtons() {
        nextButton = new TextButton("Next", skin, "default");
        nextButton.setWidth(100f);
        nextButton.setHeight(60f);
        nextButton.setPosition(
                stage.getWidth()/2-nextButton.getWidth()/2,
                stage.getHeight()/4+nextButton.getHeight()/2);
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
    
    /**
     * Shifts input data back one sample and adds a new data point to the end.
     * @param input : new data point to add
     */
    private void updateDataBuffer(float input) {
        for (int i = 0; i < inputData.length-1; i++) {
            inputData[i] = inputData[i+1];
        }
        inputData[inputData.length-1] = input;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glLineWidth(3);
        
        stage.act();
        stage.draw();
        
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        font.draw(batch, "Welcome!", 100, 150);
        font.draw(batch, "Tap anywhere to begin!", 100, 100);
        font.draw(batch, "" + (thresh-240)/240f, 100, 50);
        batch.end();
        
        if (inputData == null) {
            return;
        }
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Line);
        
        float xscale = 400/(float)inputData.length;
        float xoffset = 0;
        float yscale = 240;
        float yoffset = 240;
        
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.line(0, 240, 400, 240);
        
        shapeRenderer.setColor(LINE_COLOR[0], LINE_COLOR[1], LINE_COLOR[2], LINE_COLOR[3]);
        for (int i = 0; i < inputData.length-1; i++) {
            shapeRenderer.line(
                    i*xscale + xoffset,
                    yscale*inputData[i] + yoffset,
                    (i+1)*xscale + xoffset,
                    yscale*inputData[i+1] + yoffset);
        }
        
        if (thresh != -1) {
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.line(0, thresh, 400, thresh);
        }
        
        shapeRenderer.end();
        
        if (Gdx.input.isTouched()) {
            Vector3 v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            v = camera.unproject(v);
            if (v.y > stage.getHeight()/2) {
                thresh = (int)v.y;
            }
        }
    }
    
    public float getThreshold() {
        return (thresh - 240) / 240f;
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
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
        stage.dispose();
    }
}