var font;
var title;
var particles = [];
var fontSize = 500;
var skip_factor = 30;
var color_alpha = 30;
var window_limit = 15;
var randomOn = false;
var increaseAlpha = false;
var reverseAlpha = false;

function preload() {
   title = select("#title_animation");
   // var font_family = title.elt.style.fontFamily;
   // font = loadFont('fonts/' + font_family + '.otf');
   // font = loadFont('fonts/avenir_next_lt_pro_demi.otf');
   // fontSize = parseFloat(title.elt.style.fontSize.replace(/[^\d\.]*/g, ''));
}

function setup() {
   // var canvas = createCanvas(img.width, img.height);
   // var canvas = createCanvas(displayWidth, displayHeight);
   var canvas = createCanvas(windowWidth - window_limit, windowHeight + window_limit);
   canvas.position(0, 0);

   // var vehicle = new Particle(100, 100);
   // particles.push(vehicle);

   createParticles();
   // createParticlesFromText(title.elt.innerText, width / 2, height / 2);
}

function draw() {
   clear();
   for (var i = 0; i < particles.length; i++) {
      var v = particles[i];
      v.behaviors();
      v.update();
      v.show();
   }
}

function keyPressed() {
   if (key == 'A') {
      increaseAlpha = true;
      console.log('alpha up');
   }
}

function keyReleased() {
   if (key == 'A') {
      increaseAlpha = false;
      console.log('alpha stop');
   }
}

function mousePressed() {
   randomOn = !randomOn;
}

function windowResized() {
   particles = [];
   resizeCanvas(windowWidth - window_limit, windowHeight + window_limit);
   createParticles();
   // createParticlesFromText(title.elt.innerText, width / 2, height / 2);
}

function createParticles() {
   loadPixels();
   console.log('pixels loaded');

   for (y = 0; y < height; y += skip_factor) {
      for (x = 0; x < width; x += skip_factor) {
         var index = floor((x + y * width) * 4);
         var r = pixels[index + 0];
         var g = pixels[index + 1];
         var b = pixels[index + 2];
         var brightness = (r + g + b) / 3;

         var vehicle = new Particle(x, y);
         particles.push(vehicle);
      }
   }
   console.log(particles.length + ' particles created');
}

function createParticlesFromText(line, line_x, line_y) {
   fill(255);
   textAlign(CENTER, CENTER);
   textFont(font, fontSize);
   text(line, line_x, line_y);

   loadPixels();
   console.log('pixels loaded');

   var bounds = font.textBounds(line, line_x, line_y);

   for (y = 0; y < height; y += skip_factor) {
      for (x = 0; x < width; x += skip_factor) {
         if (x > bounds.x && x < bounds.x + bounds.w && y > bounds.y && y < bounds.y + bounds.h) {
            var index = floor((x + y * width) * 4);
            var r = pixels[index + 0];
            var g = pixels[index + 1];
            var b = pixels[index + 2];
            var brightness = (r + g + b) / 3;

            var vehicle = new Particle(x, y);
            particles.push(vehicle);

            if (brightness > 0) {
               var vehicle = new Particle(x, y);
               particles.push(vehicle);
            }
         }
      }
   }
   console.log(particles.length + ' particles created');
}