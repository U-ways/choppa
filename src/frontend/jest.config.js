module.exports = {
  moduleFileExtensions: ["js", "json", "vue"],
  transform: {
    "^.+\\.(js)?$": "babel-jest",
    "^.+\\.(vue)?$": "vue-jest",
  },
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",
  },
  setupFilesAfterEnv: [
    "jest-extended",
  ],
  testMatch: [
    "<rootDir>/src/tests/unit/**/*.test.js",
  ],
  transformIgnorePatterns: [
    "./node_modules/",
  ],
};
