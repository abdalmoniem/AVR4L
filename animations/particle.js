// Daniel Shiffman
// http://codingtra.in
// Steering Text Paths
// Video: https://www.youtube.com/watch?v=4hA7G3gup-4

function Particle(x, y) {
   this.pos = createVector(random(width), random(height));
   this.target = createVector(x, y);
   this.vel = p5.Vector.random2D();
   this.acc = createVector();
   this.r = 2;

   this.max_flee_speed = 17;
   this.max_arrive_speed = 30;

   this.max_flee_force = 5;
   this.max_arrive_force = 5;

   this.flee_distance = 50;
   this.arrive_distance = 500;
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
   var d = desired.mag();
   var speed = this.max_arrive_speed;
   if (d < this.arrive_distance) {
      speed = map(d, 0, this.arrive_distance, 0, this.max_arrive_speed);
   }
   desired.setMag(speed);
   var steer = p5.Vector.sub(desired, this.vel);
   steer.limit(this.max_arrive_force);
   return steer;
}

Particle.prototype.flee = function(target) {
   var desired = p5.Vector.sub(target, this.pos);
   var d = desired.mag();
   if (d < this.flee_distance) {
      desired.setMag(this.max_flee_speed);
      desired.mult(-1);
      var steer = p5.Vector.sub(desired, this.vel);
      steer.limit(this.max_flee_force);
      return steer;
   } else {
      return createVector(0, 0);
   }
}

Particle.prototype.applyForce = function(f) {
   this.acc.add(f);
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
   var r = random(0, 255);
   var g = random(0, 255);
   var b = random(0, 255);

   // noStroke();
   // fill(255);
   // fill(r, g, b);
   // ellipse(this.pos.x, this.pos.y, this.r);

   // stroke(255);
   stroke(r, g, b);
   strokeWeight(this.r);
   point(this.pos.x, this.pos.y);
}