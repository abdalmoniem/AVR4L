function Particle(x, y) {
   this.pos = createVector(random(width), random(height));
   this.target = createVector(x, y);
   this.vel = p5.Vector.random2D();
   this.acc = createVector();
   this.r = random(5, 40);

   this.maxFleeSpeed = 20;
   this.maxArriveSpeed = 20;

   this.maxFleeForce = 5;
   this.maxArriveForce = 5;

   this.fleeDistance = 50;
   this.arriveDistance = 50;

   this.red = random(0, 255);
   this.green = random(0, 255);
   this.blue = random(0, 255);

   this.colorShiftValue = 5;
}

Particle.prototype.behaviors = function() {
   var arrive = this.arrive(this.target);
   var mouse = createVector(mouseX, mouseY);
   var flee = this.flee(mouse);

   arrive.mult(1);
   flee.mult(20);

   this.applyForce(arrive);
   this.applyForce(flee);
}

Particle.prototype.arrive = function(target) {
   var desired = p5.Vector.sub(target, this.pos);
   var distance = desired.mag();
   var speed = this.maxArriveSpeed;
   if (distance < this.arriveDistance) {
      speed = map(distance, 0, this.arriveDistance, 0, this.maxArriveSpeed);
   }
   desired.setMag(speed);
   var steer = p5.Vector.sub(desired, this.vel);
   steer.limit(this.maxArriveForce);
   return steer;
}

Particle.prototype.flee = function(target) {
   var desired = p5.Vector.sub(target, this.pos);
   var distance = desired.mag();
   if (distance < this.fleeDistance) {
      desired.setMag(this.maxFleeSpeed);
      desired.mult(-1);
      var steer = p5.Vector.sub(desired, this.vel);
      steer.limit(this.maxFleeForce);
      return steer;
   } else {
      return createVector(0, 0);
   }
}

Particle.prototype.applyForce = function(force) {
   this.acc.add(force);
}

Particle.prototype.update = function() {
   this.pos.add(this.vel);
   this.vel.add(this.acc);

   if (this.pos.x > width || this.pos.x < 0) {
      this.vel.x *= -1;
   }

   if (this.pos.y > height || this.pos.y < 0) {
      this.vel.y *= -1;
   }

   this.acc.mult(0);
}

Particle.prototype.show = function() {
   if (randomOn) {
      this.red = (this.red + this.colorShiftValue) % 255;
      this.green = (this.green + this.colorShiftValue) % 255;
      this.blue = (this.blue + this.colorShiftValue) % 255;
   }

   if (increaseAlpha) {
      if (!reverseAlpha) {
        colorAlpha = (colorAlpha + 0.003);
      } else {
        colorAlpha = (colorAlpha - 0.003);
      }
      if (colorAlpha >= 255) {
         reverseAlpha = true;
      }
      if (colorAlpha <= 30) {
         reverseAlpha = false;
      }
   }

   if (decreaseAlpha) {
    if (!reverseAlpha) {
        colorAlpha = (colorAlpha - 0.003);
      } else {
        colorAlpha = (colorAlpha + 0.003);
      }
      if (colorAlpha <= 30) {
         reverseAlpha = true;
      }
      if (colorAlpha >= 255) {
         reverseAlpha = false;
      }
   }

   strokeWeight(this.r);
   stroke(this.red, this.green, this.blue, colorAlpha);

   // noStroke();
   // fill(255);
   // fill(this.red, this.green, this.blue);
   // ellipse(this.pos.x, this.pos.y, this.r);

   // stroke(255);

   point(this.pos.x, this.pos.y);
}