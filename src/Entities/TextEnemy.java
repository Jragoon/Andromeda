package Entities;

import Game.MainGame;
import Tools.Vector2f;
import VFX.Effect;
import VFX.EffectGenerator;
import VFX.EnemyHealthBar;
import edu.utc.game.Text;

import java.util.ArrayList;

public class TextEnemy extends Enemy {
	private boolean destinationReached;
	private int circlePosition;
	private int bulletsPerFrame;
	private boolean goingRight;
	private boolean delayed;
	private int patternTimer;
	private int patternMaxTime;
	private boolean halfAwake;
	private boolean awake;
	private boolean canFire;
	private String name;
	private Pattern pattern;

	public TextEnemy(Vector2f position, Vector2f destination, String name, int delay) {
		this.destination = destination;
		this.pos = position;
		this.hitbox.setBounds((int) pos.x, (int) pos.y, 80 + (14 * name.length()), 35);
		this.health = 300;
		this.maxHealth = 300;
		this.healthBar = new EnemyHealthBar(100, this);
		this.speed = .07f;
		this.destinationReached = false;
		this.bulletTimer = 0;
		this.bulletDelay = delay;
		this.bulletSpeed = 1.1f;
		this.bulletsPerFrame = 15;
		this.goingRight = true;
		this.delayed = false;
		this.patternTimer = 0;
		this.patternMaxTime = 3000;
		this.halfAwake = false;
		this.awake = false;
		this.canFire = false;
		this.points = name.length() * 1000;
		this.pattern = Pattern.OCTO;
		this.name = name;
	}

	@Override
	public void update(int delta) {
		bulletTimer += delta;
		patternTimer += delta;
		healthBar.update(delta);
		if (health < maxHealth) canFire = true;
		if (patternTimer >= patternMaxTime) {
			switchPattern();
		}
		if (health <= 0) die();
		else if (health < (maxHealth * .5f) && !awake) {
			//increaseDifficulty();
			moveBottomTop();
			awake = true;
		}
		else if (health < (maxHealth * .75f) && !halfAwake) {
			//increaseDifficulty();
			moveMiddle();
			halfAwake = true;
		}
		if (!destinationReached) {
			goToDestination(delta);
			destinationReached = pos.distanceTo(destination) <= 1f;
			if (destinationReached) destination = pos.add(new Vector2f(0, 500));
		}
		if (!delayed && bulletTimer >= bulletDelay) {
			delayed = true;
			bulletTimer = 0;
		}
		if (delayed && bulletTimer >= bulletDelay) {
			delayed = false;
			bulletTimer = 0;
		}
		if (bulletTimer < bulletDelay && !delayed) {
			if (canFire) fire();
		}

		adjustHitBox();
		if (goingRight && hitbox.x + hitbox.width >= Game.MainGame.ui.getWidth()) {
			goingRight = false;
		}
		else if (!goingRight && hitbox.x <= 0) {
			goingRight = true;
		}
		if (goingRight && destinationReached) {
			destination = new Vector2f(pos);
			destination.x += 100;
			goToDestination(delta);
		}
		if (!goingRight && destinationReached) {
			destination = new Vector2f(pos);
			destination.x -= 100;
			goToDestination(delta);
		}
	}

	private void fire() {
		switch (pattern) {
			case STAR:
				fireStar();
				break;
			case OCTO:
				fireOcto();
				break;
			case CIRCLE:
				fireCircle();
				break;
		}
	}

	private void switchPattern() {
		switch (pattern) {
			case STAR:
				pattern = Pattern.OCTO;
				break;
			case OCTO:
				pattern = Pattern.STAR;
				break;
		}
		patternTimer = 0;
	}

	private void increaseDifficulty() {
		speed *= 1.5f;
		bulletDelay *= 1.1f;
		patternMaxTime /= 1.5f;
		bulletSpeed *= 1.1f;
	}

	private void moveMiddle() {
		destinationReached = false;
		destination = new Vector2f((MainGame.ui.getWidth()/2f)-35, (MainGame.ui.getHeight()/2f)-100);
	}

	private void moveBottomTop() {
		destinationReached = false;
		destination = new Vector2f((MainGame.ui.getWidth()/2f)-35, 100);
	}

	private void fireStar() {
		Vector2f center = new Vector2f(pos.x + hitbox.width / 2f, pos.y + hitbox.height / 2f);
		for (int i = 0; i < bulletsPerFrame; i++) {
			circlePosition += 25;
			if (circlePosition > 360) circlePosition = 0;
			float x = (hitbox.width/4f) * (float) Math.cos(circlePosition * Math.PI / 180);
			float y = (hitbox.width/4f) * (float) Math.sin(circlePosition * Math.PI / 180);
			Vector2f position = new Vector2f(x + center.x, y + center.y);
			Vector2f direction = (position.subtract(center));
			direction.normalize();
			Bullet bullet = new Bullet(position, direction, bulletSpeed);
			MainGame.enemyBullets.add(bullet);
		}
	}

	private void fireCircle() {
		Vector2f center = new Vector2f(pos.x + hitbox.width / 2f, pos.y + hitbox.height / 2f);
		for (int i = 0; i < 360; i++) {
			float x = (hitbox.width/2f) * (float) Math.cos(i * Math.PI / 180);
			float y = (hitbox.width/2f) * (float) Math.sin(i * Math.PI / 180);
			Vector2f position = new Vector2f(x + center.x, y + center.y);
			Vector2f direction = (position.subtract(center));
			direction.normalize();
			Bullet bullet = new Bullet(position, direction, bulletSpeed);
			MainGame.enemyBullets.add(bullet);
		}
	}

	private void fireOcto() {
		Vector2f center = new Vector2f(pos.x - 5 + hitbox.width / 2f, pos.y - 5 + hitbox.height / 2f);
		ArrayList<Vector2f> positions = new ArrayList<>();
		float outside = hitbox.width / 6f;
		positions.add(new Vector2f(center.x - outside, center.y));
		positions.add(new Vector2f(center.x + outside, center.y));
		positions.add(new Vector2f(center.x - outside, center.y - outside));
		positions.add(new Vector2f(center.x + outside, center.y + outside));
		positions.add(new Vector2f(center.x - outside, center.y + outside));
		positions.add(new Vector2f(center.x + outside, center.y - outside));
		positions.add(new Vector2f(center.x, center.y + outside));
		positions.add(new Vector2f(center.x, center.y - outside));
		for (Vector2f position : positions) {
			Vector2f direction = (position.subtract(center));
			direction.normalize();
			Bullet bullet = new Bullet(position, direction, bulletSpeed);
			MainGame.enemyBullets.add(bullet);
		}
	}

	private void goToDestination(int delta) {
		Vector2f direction = destination.subtract(pos);
		direction.normalize();
		pos.x += direction.x * speed * delta;
		pos.y += direction.y * speed * delta;
	}

	private void adjustHitBox() {
		hitbox.x = (int) pos.x;
		hitbox.y = (int) pos.y;
	}

	@Override
	protected void die() {
		deactivate();
		Effect explode = EffectGenerator.generateDeathExplosion(this);
		MainGame.effects.add(explode);
	}

	@Override
	public void draw() {
		healthBar.draw();
		new Text((int) pos.x - 20, (int) pos.y - 15, 40, 30, name).draw();
	}
}