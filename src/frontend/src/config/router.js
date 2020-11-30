import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    component: () => import("@/pages/HomePage"),
  },
  {
    path: "/tribes/:id",
    component: () => import("@/components/templates/StandardPageTemplate"),
  },
  {
    path: "/external/github",
    beforeEnter() { window.location.href = "https://github.com/u-ways/chopper"; },
  },
  {
    path: "/external/production",
    beforeEnter() { window.location.href = "https://www.choppa.app"; },
  },
  {
    path: "/external/staging",
    beforeEnter() { window.location.href = "https://choppa-staging.herokuapp.com/"; },
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import("@/pages/NotFoundPage"),
  },
];

const router = new VueRouter({
  routes,
});

export default router;
