var font;
var bounds;
var particles = [];
var fontSize = 500;
var skip_factor = 8;
var window_limit = 20;

function preload() {
   // img = loadImage('avr4l_small.png');
   font = loadFont('fonts/avenir_next_lt_pro_demi.otf');
}

function setup() {
   // var canvas = createCanvas(img.width, img.height);
   // var canvas = createCanvas(windowWidth - window_limit, windowHeight - window_limit);
   var canvas = createCanvas(displayWidth, displayHeight);
   canvas.position(0, 0);
   clear();
   var btn = select("#learn_more_btn");
   var btn_pos_y = btn.position().y;
   createParticlesFromText('AVR4L', windowWidth / 2, btn_pos_y + 150);
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

function windowResized() {
   particles = [];
   resizeCanvas(windowWidth - window_limit, windowHeight - window_limit);
   var btn = select("#learn_more_btn");
   var btn_pos_y = btn.position().y;
   createParticlesFromText('AVR4L', windowWidth / 2, btn_pos_y);
}

function createParticlesFromText(line, line_x, line_y) {
   fill(255);
   textAlign(CENTER, CENTER);
   textFont(font, fontSize);
   text(line, line_x, line_y);

   loadPixels();
   console.log('pixels loaded');

   var bounds = font.textBounds(line, line_x, line_y);

   fill(0, 0, 255);
   for (y = 0; y < height; y += skip_factor) {
      for (x = 0; x < width; x += skip_factor) {
         if (x > bounds.x && x < bounds.x + bounds.w && y > bounds.y && y < bounds.y + bounds.h) {
            var index = floor((x + y * width) * 4);
            var r = pixels[index + 0];
            var g = pixels[index + 1];
            var b = pixels[index + 2];
            var brightness = (r + g + b) / 3;

            if (brightness > 0) {
               var vehicle = new Particle(x, y);
               particles.push(vehicle);
            }
         }
      }
   }
   console.log(particles.length + ' particles created');
}
