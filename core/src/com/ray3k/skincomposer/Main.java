/*******************************************************************************
 * MIT License
 * 
 * Copyright (c) 2021 Raymond Buckley
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.ray3k.skincomposer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane.SplitPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip.TextTooltipStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.TreeStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ray3k.skincomposer.data.AtlasData;
import com.ray3k.skincomposer.data.JsonData;
import com.ray3k.skincomposer.data.ProjectData;
import com.ray3k.skincomposer.dialog.DialogFactory;
import com.ray3k.skincomposer.dialog.DialogListener;
import com.ray3k.skincomposer.utils.Utils;
import com.ray3k.stripe.FreeTypeSkin;
import com.ray3k.stripe.ScrollFocusListener;
import com.ray3k.tenpatch.TenPatchDrawable;
import space.earlygrey.shapedrawer.GraphDrawer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Main extends ApplicationAdapter {
    public final static String VERSION = "46";
    public static String newVersion;
    public static final Class[] BASIC_CLASSES = {Button.class, CheckBox.class,
        ImageButton.class, ImageTextButton.class, Label.class, List.class,
        ProgressBar.class, ScrollPane.class, SelectBox.class, Slider.class,
        SplitPane.class, TextButton.class, TextField.class, TextTooltip.class,
        Touchpad.class, Tree.class, Window.class};
    public static final Class[] STYLE_CLASSES = {ButtonStyle.class,
        CheckBoxStyle.class, ImageButtonStyle.class, ImageTextButtonStyle.class,
        LabelStyle.class, ListStyle.class, ProgressBarStyle.class,
        ScrollPaneStyle.class, SelectBoxStyle.class, SliderStyle.class,
        SplitPaneStyle.class, TextButtonStyle.class, TextFieldStyle.class,
        TextTooltipStyle.class, TouchpadStyle.class, TreeStyle.class,
        WindowStyle.class};
    public static Stage stage;
    public static Skin skin;
    public static ScreenViewport viewport;
    public static ShapeDrawer shapeDrawer;
    public static GraphDrawer graphDrawer;
    public static DialogFactory dialogFactory;
    public static DesktopWorker desktopWorker;
    public static TenPatchDrawable loadingAnimation;
    public static TenPatchDrawable loadingAnimation2;
    public static UndoableManager undoableManager;
    public static ProjectData projectData;
    public static JsonData jsonData;
    public static AtlasData atlasData;
    public static RootTable rootTable;
    public static IbeamListener ibeamListener;
    public static MainListener mainListener;
    public static HandListener handListener;
    public static ScrollFocusListener scrollFocusListener;
    public static ResizeArrowListener verticalResizeArrowListener;
    public static ResizeArrowListener horizontalResizeArrowListener;
    public static TooltipManager tooltipManager;
    public static FileHandle appFolder;
    private String[] args;
    public static Main main;
    
    public Main (String[] args) {
        this.args = args;
        main = this;
    }
    
    @Override
    public void create() {
        appFolder = Gdx.files.external(".skincomposer/");
        
        skin = new FreeTypeSkin(Gdx.files.internal("skin-composer-ui/skin-composer-ui.json"));
        viewport = new ScreenViewport();
//        viewport.setUnitsPerPixel(.5f);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        
        shapeDrawer = new ShapeDrawer(stage.getBatch(), skin.getRegion("white"));
        graphDrawer = new GraphDrawer(shapeDrawer);
        
        initDefaults();
        
        populate();
    
        resizeUiScale(projectData.getUiScale());
    }
    
    private void initDefaults() {
        if (Utils.isMac()) System.setProperty("java.awt.headless", "true");
        
        skin.getFont("font").getData().markupEnabled = true;
        
        //copy defaults.json to temp folder if it doesn't exist
        var fileHandle = appFolder.child("texturepacker/atlas-export-settings.json");
        if (!fileHandle.exists()) {
            Gdx.files.internal("atlas-export-settings.json").copyTo(fileHandle);
        }
        
        //copy atlas settings for preview to temp folder if it doesn't exist
        fileHandle = appFolder.child("texturepacker/atlas-internal-settings.json");
        if (!fileHandle.exists()) {
            Gdx.files.internal("atlas-internal-settings.json").copyTo(fileHandle);
        }
    
        //copy white-pixel.png for pixel drawables
        fileHandle = appFolder.child("texturepacker/white-pixel.png");
        if (!fileHandle.exists()) {
            Gdx.files.internal("white-pixel.png").copyTo(fileHandle);
        }
        
        //copy preview fonts to preview fonts folder if they do not exist
        fileHandle = appFolder.child("preview fonts/IBMPlexSerif-Medium.ttf");
        if (!fileHandle.exists()) {
            Gdx.files.internal("preview fonts/IBMPlexSerif-Medium.ttf").copyTo(fileHandle);
        }
        
        fileHandle = appFolder.child("preview fonts/Pacifico-Regular.ttf");
        if (!fileHandle.exists()) {
            Gdx.files.internal("preview fonts/Pacifico-Regular.ttf").copyTo(fileHandle);
        }
        
        fileHandle = appFolder.child("preview fonts/PressStart2P-Regular.ttf");
        if (!fileHandle.exists()) {
            Gdx.files.internal("preview fonts/PressStart2P-Regular.ttf").copyTo(fileHandle);
        }
        
        fileHandle = appFolder.child("preview fonts/SourceSansPro-Regular.ttf");
        if (!fileHandle.exists()) {
            Gdx.files.internal("preview fonts/SourceSansPro-Regular.ttf").copyTo(fileHandle);
        }
        
        ibeamListener = new IbeamListener();
        
        dialogFactory = new DialogFactory();
        projectData = new ProjectData();
        projectData.randomizeId();
        projectData.setMaxUndos(30);
        atlasData = projectData.getAtlasData();
        jsonData = projectData.getJsonData();
        
        newVersion = VERSION;
        if (projectData.isCheckingForUpdates()) {
            checkForUpdates(this);
        }
        
        undoableManager = new UndoableManager(this);
        
        desktopWorker.attachLogListener();
        desktopWorker.setCloseListener(() -> {
            dialogFactory.showCloseDialog(new DialogListener() {
                @Override
                public void opened() {
                    desktopWorker.removeFilesDroppedListener(rootTable.getFilesDroppedListener());
                }

                @Override
                public void closed() {
                    desktopWorker.addFilesDroppedListener(rootTable.getFilesDroppedListener());
                }
            });
            return false;
        });
        
        loadingAnimation = skin.get("loading-animation", TenPatchDrawable.class);
        loadingAnimation2 = skin.get("loading-animation2", TenPatchDrawable.class);
        
        projectData.getAtlasData().clearTempData();
        handListener = new HandListener();
        
        scrollFocusListener = new ScrollFocusListener(stage);
        
        verticalResizeArrowListener = new ResizeArrowListener(true);
        horizontalResizeArrowListener = new ResizeArrowListener(false);
        
        tooltipManager = new TooltipManager();
        tooltipManager.animations = false;
        tooltipManager.initialTime = .4f;
        tooltipManager.resetTime = 0.0f;
        tooltipManager.subsequentTime = 0.0f;
        tooltipManager.hideAll();
        tooltipManager.instant();
    }

    private void populate() {
        stage.clear();
        
        rootTable = new RootTable();
        rootTable.setFillParent(true);
        mainListener = new MainListener();
        rootTable.addListener(mainListener);
        rootTable.populate();
        stage.addActor(rootTable);
        rootTable.updateRecentFiles();
        
        //pass arguments
        if (!mainListener.argumentsPassed(args)) {
            //show welcome screen if there are no valid arguments
            mainListener.createWelcomeListener();
        }
    }
    
    @Override
    public void render() {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act(Gdx.graphics.getDeltaTime());
        
        for (var tenPatch : skin.getAll(TenPatchDrawable.class)) {
            tenPatch.value.update(Gdx.graphics.getDeltaTime());
        }
        
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width != 0 && height != 0) {
            stage.getViewport().update(width, height, true);
            rootTable.fire(new StageResizeEvent(width, height));
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
    
    public void resizeUiScale(int scale) {
        resizeUiScale(scale, scale > 1);
    }
    
    public void resizeUiScale(int scale, boolean large) {
        if (large) {
            desktopWorker.sizeWindowToFit(1440, 1440, 50, Gdx.graphics);
        } else {
            desktopWorker.sizeWindowToFit(800, 800, 50, Gdx.graphics);
        }
        desktopWorker.centerWindow(Gdx.graphics);
        viewport.setUnitsPerPixel(1f / scale);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public static Class basicToStyleClass(Class clazz) {
        int i = 0;
        for (Class basicClass : BASIC_CLASSES) {
            if (clazz.equals(basicClass)) {
                break;
            } else {
                i++;
            }
        }
        return i < STYLE_CLASSES.length ? STYLE_CLASSES[i] : null;
    }
    
    public static Class styleToBasicClass(Class clazz) {
        int i = 0;
        for (Class styleClass : STYLE_CLASSES) {
            if (clazz.equals(styleClass)) {
                break;
            } else {
                i++;
            }
        }
        return BASIC_CLASSES[i];
    }

    public static void checkForUpdates(Main main) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
                HttpRequest httpRequest = requestBuilder.newRequest().method(HttpMethods.GET).url("https://raw.githubusercontent.com/raeleus/skin-composer/master/version").build();
                Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
                    @Override
                    public void handleHttpResponse(Net.HttpResponse httpResponse) {
                        newVersion = httpResponse.getResultAsString();
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                main.rootTable.fire(new RootTable.RootTableEvent(RootTable.RootTableEnum.CHECK_FOR_UPDATES_COMPLETE));
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable t) {
                        newVersion = VERSION;
                    }

                    @Override
                    public void cancelled() {
                        newVersion = VERSION;
                    }
                });
            }
        });
        
        thread.start();
    }
}