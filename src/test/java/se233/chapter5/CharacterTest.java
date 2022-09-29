package se233.chapter5;

import javafx.embed.swing.JFXPanel;
import javafx.scene.input.KeyCode;
import org.junit.Before;
import org.junit.Test;
import se233.chapter5.controller.DrawingLoop;
import se233.chapter5.controller.GameLoop;
import se233.chapter5.model.Character;
import se233.chapter5.view.Platform;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class CharacterTest {
    private Character floatingCharacter, floatingCharacter2;
    private ArrayList<Character> characterListUnderTest;
    private Platform platformUnderTest;
    private GameLoop gameLoopUnderTest;
    private DrawingLoop drawingLoopUnderTest;
    private Method updateMethod, redrawMethod;
    @Before
    public void setup() {
        JFXPanel jfxPanel = new JFXPanel();
        floatingCharacter = new Character(30,30,0,0, KeyCode.A,KeyCode.D,KeyCode.W);
        floatingCharacter2 = new Character(Platform.WIDTH-60,30,0,0, KeyCode.LEFT,KeyCode.RIGHT,KeyCode.UP);
        characterListUnderTest = new ArrayList<>();
        characterListUnderTest.add(floatingCharacter);
        characterListUnderTest.add(floatingCharacter2);
        platformUnderTest = new Platform();
        gameLoopUnderTest = new GameLoop(platformUnderTest);
        drawingLoopUnderTest = new DrawingLoop(platformUnderTest);
        try {
            updateMethod = GameLoop.class.getDeclaredMethod("update", ArrayList.class);
            redrawMethod = DrawingLoop.class.getDeclaredMethod("paint", ArrayList.class);
            updateMethod.setAccessible(true);
            redrawMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            updateMethod = null;
            redrawMethod = null;
        }
    }
    @Test
    public void characterInitialValueShouldMatchConstructorArguments() {
        assertEquals("Initial x", 30,floatingCharacter.getX(),0);
        assertEquals("Initial y", 30,floatingCharacter.getY(),0);
        assertEquals("Offset x",0,floatingCharacter.getOffsetX(),0.0);
        assertEquals("Offset Y",0,floatingCharacter.getOffsetY(),0.0);
        assertEquals("Left key",KeyCode.A,floatingCharacter.getLeftKey());
        assertEquals("Right key",KeyCode.D,floatingCharacter.getRightKey());
        assertEquals("Up key",KeyCode.W,floatingCharacter.getUpKey());
    }
    @Test
    public void characterShouldMoveToTheLeftAfterTheLeftKeyIsPressed() throws IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        Character characterUnderTest = characterListUnderTest.get(0);
        int startX = characterUnderTest.getX();
        platformUnderTest.getKeys().add(KeyCode.A);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        Field isMoveLeft = characterUnderTest.getClass().getDeclaredField("isMoveLeft");
        isMoveLeft.setAccessible(true);
        assertTrue("Controller: Left key pressing is acknowledged", platformUnderTest.getKeys().isPressed(KeyCode.A));
        assertTrue("Model: Character moving left state is set", isMoveLeft.getBoolean(characterUnderTest));
        assertTrue("View: Character is moving left", characterUnderTest.getX() < startX);
    }
    // EXERCISE 1
    @Test
    public void characterMoveRightAtRightSpeedAfterTheRightKeyIsPressed() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        Character characterUnderTest = characterListUnderTest.get(0);
        platformUnderTest.getKeys().add(KeyCode.D);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        Field isMoveRight = characterUnderTest.getClass().getDeclaredField("isMoveRight");
        isMoveRight.setAccessible(true);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(250);
            updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
            redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        }
        assertTrue("Controller: Right key pressing is acknowledged", platformUnderTest.getKeys().isPressed(KeyCode.D));
        assertTrue("Model: Character moving right state is set", isMoveRight.getBoolean(characterUnderTest));
        assertEquals("Model: Character moving right at a correct velocity", 7, characterUnderTest.getxVelocity());
    }
    // EXERCISE 2
    @Test
    public void characterJumpWhenOnTheGroundAfterTheUpKeyIsPressed() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Character characterUnderTest = characterListUnderTest.get(0);
        characterUnderTest.setY(Platform.GROUND - Character.CHARACTER_HEIGHT);
        characterUnderTest.checkReachFloor();
        Field canJump = characterUnderTest.getClass().getDeclaredField("canJump");
        canJump.setAccessible(true);
        assertTrue("Model: Character can jump/is on the ground", canJump.getBoolean(characterUnderTest));
        platformUnderTest.getKeys().add(KeyCode.W);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        Field isJumping = characterUnderTest.getClass().getDeclaredField("isJumping");
        isJumping.setAccessible(true);
        assertTrue("Controller: Up key pressing is acknowledged", platformUnderTest.getKeys().isPressed(KeyCode.W));
        assertTrue("Model: Character is in jumping state", isJumping.getBoolean(characterUnderTest));
    }
    // EXERCISE 3
    @Test
    public void characterJumpWhenNotOnTheGroundAfterTheUpKeyIsPressed() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Character characterUnderTest = characterListUnderTest.get(0);
        characterUnderTest.checkReachFloor();
        Field canJump = characterUnderTest.getClass().getDeclaredField("canJump");
        canJump.setAccessible(true);
        assertFalse("Model: Character cannot jump/is not on the ground", canJump.getBoolean(characterUnderTest));
        platformUnderTest.getKeys().add(KeyCode.W);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        Field isJumping = characterUnderTest.getClass().getDeclaredField("isJumping");
        isJumping.setAccessible(true);
        assertTrue("Controller: Up key pressing is acknowledged", platformUnderTest.getKeys().isPressed(KeyCode.W));
        assertFalse("Model: Character is not in jumping state", isJumping.getBoolean(characterUnderTest));
    }
    // EXERCISE 4
    @Test
    public void characterReachGameWallAfterHoldingLeftKey() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Character characterUnderTest = characterListUnderTest.get(0);
        characterUnderTest.setX(1);
        platformUnderTest.getKeys().add(KeyCode.A);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        Field isMoveLeft = characterUnderTest.getClass().getDeclaredField("isMoveLeft");
        isMoveLeft.setAccessible(true);
        assertTrue("Controller: Left key pressing is acknowledged", platformUnderTest.getKeys().isPressed(KeyCode.A));
        assertTrue("Model: Character moving left state is set", isMoveLeft.getBoolean(characterUnderTest));
        platformUnderTest.getKeys().add(KeyCode.A);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        characterUnderTest.checkReachGameWall();
        assertEquals("Model: Character stops at a border", 0, characterUnderTest.getX());
    }
    // EXERCISE 5
    @Test
    public void character1CollidesWithCharacter2AfterCharacter2LeftKeyIsPressed() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Character characterUnderTest = characterListUnderTest.get(0);
        Character characterUnderTest2 = characterListUnderTest.get(1);
        characterUnderTest2.setX(63);
        platformUnderTest.getKeys().add(KeyCode.LEFT);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        Field isMoveLeft = characterUnderTest2.getClass().getDeclaredField("isMoveLeft");
        isMoveLeft.setAccessible(true);
        assertTrue("Controller: Left (arrow) key pressing is acknowledged", platformUnderTest.getKeys().isPressed(KeyCode.LEFT));
        assertTrue("Model: Character 2 moving left state is set", isMoveLeft.getBoolean(characterUnderTest2));
        assertEquals("Model: Character 2 stops at Character 1", characterUnderTest.getX()+Character.CHARACTER_WIDTH, characterUnderTest2.getX());
        assertEquals("Model: Character 1 stops at Character 2", characterUnderTest2.getX(), characterUnderTest.getX()+Character.CHARACTER_WIDTH);
    }
    // EXERCISE 6
    @Test
    public void character2RespawnedAfterCharacter1Stomped() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Character characterUnderTest = characterListUnderTest.get(0);
        Character characterUnderTest2 = characterListUnderTest.get(1);
        characterUnderTest2.setX(characterUnderTest.getX());
        characterUnderTest2.setY(characterUnderTest.getY()+Character.CHARACTER_HEIGHT);
        redrawMethod.invoke(drawingLoopUnderTest, characterListUnderTest);
        updateMethod.invoke(gameLoopUnderTest, characterListUnderTest);
        characterUnderTest.collided(characterUnderTest2);
        characterUnderTest2.collided(characterUnderTest);
        Field startX = characterUnderTest2.getClass().getDeclaredField("startX");
        startX.setAccessible(true);
        Field startY = characterUnderTest2.getClass().getDeclaredField("startY");
        startY.setAccessible(true);
        assertEquals("Controller: Character 2 respawned at initial X after stomped by Character 1", startX.get(characterUnderTest2), characterUnderTest2.getX());
        assertEquals("Controller: Character 2 respawned at initial Y after stomped by Character 1", startY.get(characterUnderTest2), characterUnderTest2.getY());
        assertEquals("Views: Character 1's score is increased by 1", 1, characterUnderTest.getScore());
    }
}