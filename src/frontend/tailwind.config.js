const colors = require("tailwindcss/colors");
const tailWindCssDebugScreens = require("tailwindcss-debug-screens");

module.exports = {
  purge: [
    "./public/**/*.html",
    "./src/**/*.vue",
    "./src/**/*.css",
  ],
  darkMode: "class",
  theme: {
    extend: {
      animation: {
        "fade-in": "fade-in 0.2s",
        "slide-left": "slide-left 1s",
      },
      colors: {
        orange: colors.orange,
        amber: colors.amber,
        emerald: colors.emerald,
        rose: colors.rose,
        gray: {
          666: "#444e5d",
          ...colors.coolGray,
        },
        "choppa-light": {
          extra: colors.emerald["500"],
          DEFAULT: colors.emerald["700"],
          hover: colors.emerald["800"],
        },
        "choppa-dark": {
          DEFAULT: colors.emerald["500"],
          hover: colors.emerald["600"],
        },
        "choppa-two": "#6762d9",
      },
      fontFamily: {
        "open-sans": ["Open\\ Sans", "sans-serif"],
      },
      keyframes: {
        "fade-in": {
          "0%": { opacity: 0 },
          "100%": { opacity: 1 },
        },
        "slide-left": {
          "0%": { transform: "translateX(0%)" },
          "100%": { transform: "translateX(-100%)" },
        },
      },
      minHeight: {
        72: "18rem",
      },
      spacing: {
        192: "48rem",
        120: "30rem",
      },
    },
    container: {
      screens: {
        sm: "2640px",
        md: "768px",
        lg: "1024px",
        xl: "1280px",
      },
    },
  },
  variants: {
    extend: {
      backgroundColor: ["even"],
      fontStyle: ["focus"],
      outline: ["active", "focus"],
      ringWidth: ["hover", "focus", "active"],
    },
  },
  plugins: [
    tailWindCssDebugScreens,
  ],
};
