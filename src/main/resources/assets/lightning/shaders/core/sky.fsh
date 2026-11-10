#version 330

out vec4 fragColor;

// NOTE: layout must stay identical to what SkyShader.java writes (std140).
layout(std140) uniform SkySettings {
    vec4 Tint;
    vec4 Color1;
    vec4 Color2;
    vec4 CameraAngles;
    vec2 Resolution;
    float Time;
    float Intensity;
    float Speed;
    int Mode;
};

/* =====================================================================
 *  Lightning sky  (Mode 0 = Thunder, Mode 1 = Storm)
 *
 *  Designed to be cheap:
 *   - no sin()/cos() based hashing, only fract() math
 *   - clouds are only 3 octaves of value noise (1 fbm per pixel)
 *   - the lightning bolt is evaluated ONLY while a strike is happening
 *     (the branch depends on uniforms only, so the whole screen takes
 *      the same path and the GPU does not diverge)
 * ===================================================================== */

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash12(i);
    float b = hash12(i + vec2(1.0, 0.0));
    float c = hash12(i + vec2(0.0, 1.0));
    float d = hash12(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// 3 octaves, result in 0..1
float clouds(vec2 p) {
    float v = 0.5 * vnoise(p);
    p = p * 2.03 + 17.0;
    v += 0.25 * vnoise(p);
    p = p * 2.01 + 31.0;
    v += 0.125 * vnoise(p);
    return v * (1.0 / 0.875);
}

// 1D piecewise-linear noise -> sharp zig-zags, range -0.5..0.5
float jag(float y, float seed) {
    float u = y + seed;
    float i = floor(u);
    return mix(hash11(i), hash11(i + 1.0), fract(u)) - 0.5;
}

// horizontal position of the main bolt at height y
float boltX(float y, float x0, float seed) {
    return x0
         + 0.34  * jag(y * 5.0,  seed)
         + 0.12  * jag(y * 14.0, seed + 11.0)
         + 0.035 * jag(y * 38.0, seed + 23.0);
}

// flash envelope of one strike cycle: sharp hit + two flickers
float flashEnv(float t, float period, float salt, out float cell, out float dt) {
    float cf = t / period;
    cell = floor(cf);
    float ft = (cf - cell) * period;
    float s0 = 0.35 + hash11(cell * 1.7 + salt + 3.1) * (period - 2.8);
    dt = ft - s0;
    if (dt < 0.0) return 0.0;
    float e = exp(-dt * 9.0);
    e += 0.75 * step(0.13, dt) * exp(-(dt - 0.13) * 11.0);
    e += 0.45 * step(0.30, dt) * exp(-(dt - 0.30) * 7.0);
    return e;
}

void main() {
    vec2 res = Resolution;
    vec2 frag = gl_FragCoord.xy;
    vec2 p = (frag - 0.5 * res) / res.y;      // y in -0.5..0.5, aspect-correct x
    float aspect = res.x / res.y;
    float uvy = frag.y / res.y;               // 0 = bottom, 1 = top

    bool storm = (Mode == 1);
    float t = Time;                           // Java already applies the Speed setting to Time
    float gain = clamp(Intensity, 0.6, 1.4);

    /* ---------- palette ---------- */
    vec3 horizon = storm ? vec3(0.030, 0.034, 0.050) : vec3(0.034, 0.016, 0.066);
    vec3 zenith  = storm ? vec3(0.004, 0.006, 0.011) : vec3(0.003, 0.003, 0.018);
    vec3 cloudC  = storm ? vec3(0.034, 0.038, 0.052) : vec3(0.030, 0.024, 0.066);
    vec3 boltC   = storm ? vec3(0.85, 0.93, 1.00)    : vec3(0.82, 0.76, 1.00);
    vec3 glowC   = storm ? vec3(0.34, 0.50, 0.95)    : vec3(0.48, 0.30, 1.00);

    /* ---------- clouds (the only per-pixel noise) ---------- */
    float drift = storm ? 0.085 : 0.045;
    vec2 cp = vec2(p.x * 1.9 + t * drift, p.y * 2.7 + 0.03 * sin(t * 0.2));
    float n = clouds(cp);
    float dens = smoothstep(storm ? 0.30 : 0.38, 0.78, n);
    dens *= mix(0.55, 1.0, smoothstep(-0.55, 0.45, p.y));   // thicker overhead

    vec3 col = mix(horizon, zenith, smoothstep(0.0, 1.0, uvy));
    col = mix(col, cloudC * (0.6 + 0.8 * n), dens * 0.92);

    /* ---------- main strike ---------- */
    float cell;
    float dt;
    float period = storm ? 3.2 : 6.5;
    float flash = flashEnv(t, period, 0.0, cell, dt);
    float x0 = (hash11(cell * 2.3 + 1.1) - 0.5) * aspect * 0.8;

    if (flash > 0.0) {
        float lit = flash * gain;
        float prox = 0.35 + 0.65 * exp(-abs(p.x - x0) * 1.6);
        // sky + clouds lit from inside / behind
        col += glowC * lit * prox * (0.03 + dens * (0.20 + 0.40 * n));
    }

    if (flash > 0.02) {
        float seed = cell * 13.7;
        float yEnd = -0.10 - hash11(cell * 3.1 + 5.5) * 0.30;
        float prog = clamp(dt / 0.07, 0.0, 1.0);             // bolt "falls" in 70 ms
        float front = mix(0.55, yEnd, prog);
        float vis = smoothstep(front - 0.03, front + 0.02, p.y)
                  * smoothstep(yEnd - 0.05, yEnd + 0.10, p.y);

        // main bolt, with slope correction so steep zig-zags keep their width
        float xb  = boltX(p.y, x0, seed);
        float xb2 = boltX(p.y + 0.004, x0, seed);
        float slope = (xb2 - xb) * 250.0;
        float dh = abs(p.x - xb);
        float d = dh * inversesqrt(1.0 + slope * slope);   // core only: glow uses plain dh
        float w = mix(0.0030, 0.0062, uvy);
        float bolt = (1.0 - smoothstep(0.0, w, d)) + 0.45 * exp(-dh * 22.0);

        // one side branch
        float yb  = 0.10 + 0.22 * hash11(cell * 4.7 + 2.2);
        float xbr = boltX(yb, x0, seed);
        float dir = hash11(cell * 5.9 + 9.9) > 0.5 ? 1.0 : -1.0;
        float tb  = yb - p.y;
        float xbb = xbr + dir * tb * 0.50 + 0.07 * jag(p.y * 9.0, seed + 41.0);
        float db  = abs(p.x - xbb) * 0.82;
        float bm  = smoothstep(0.0, 0.03, tb) * (1.0 - smoothstep(0.14, 0.26, tb));
        bolt += ((1.0 - smoothstep(0.0, w * 0.7, db)) + 0.20 * exp(-db * 30.0)) * bm * 0.8;

        bolt *= vis;
        col += mix(glowC, boltC, clamp(bolt, 0.0, 1.0)) * bolt * flash * gain * 1.3;
    }

    /* ---------- storm extras: hidden sheet lightning + rain ---------- */
    if (storm) {
        float cell2;
        float dt2;
        float f2 = flashEnv(t * 0.93 + 11.0, 2.7, 5.0, cell2, dt2);
        if (f2 > 0.0) {
            vec2 gp = vec2((hash11(cell2 * 1.9 + 4.4) - 0.5) * aspect, 0.15);
            float g = exp(-length(p - gp) * 1.4);
            col += glowC * f2 * gain * g * (0.02 + dens * 0.25);
        }

        // thin slanted rain streaks (2 cheap hashes per pixel)
        vec2 rp = vec2(frag.x + frag.y * 0.25, frag.y);
        float ci = floor(rp.x / (res.y * 0.003));
        float h = hash11(ci * 1.31);
        float v = fract(uvy * (1.5 + h * 1.5) + t * (1.2 + h) + h * 7.0);
        float streak = smoothstep(0.0, 0.02, v) * (1.0 - smoothstep(0.02, 0.10, v));
        col += vec3(0.08, 0.10, 0.14) * streak * step(0.90, h);
    }

    /* ---------- output ---------- */
    col *= Tint.rgb;
    col = 1.0 - exp(-col * 1.5);
    col = sqrt(col);
    col += (hash12(frag) - 0.5) * (1.5 / 255.0);   // kills banding in dark gradients
    fragColor = vec4(col, 1.0);
}
