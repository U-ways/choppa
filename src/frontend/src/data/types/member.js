export default class Member {
  constructor(config) {
    this._id = Object.prototype.hasOwnProperty.call(config, "id") ? config.id : "";
    this._name = Object.prototype.hasOwnProperty.call(config, "name") ? config.name : "";
    this._chapter = Object.prototype.hasOwnProperty.call(config, "chapter") ? config.chapter : null;
  }

  get id() {
    return this._id;
  }

  get name() {
    return this._name;
  }

  set name(newName) {
    this._name = newName;
  }

  set chapter(newChapter) {
    this._chapter = newChapter;
  }

  get chapter() {
    return this._chapter;
  }
}
