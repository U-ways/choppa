<template>
  <div class="flex flex-col gap-2 text-center">
    <img :src="require('@/assets/svg/no-members.svg')" alt="This squad has no members" width="100%"
         height="100%" class="h-28 w-auto mx-auto"/>
    <p class="place-self-center font-semibold text-md dark:text-gray-300">
      Add your first member to this Squad.
    </p>
    <div class="inline-block mx-auto">
      <StyledButton type="link" :link="{ name: 'create-member', query: { squad: squad.path } }" variant="primary">
        Add Member
      </StyledButton>
    </div>
  </div>
</template>

<script>
import StyledButton from "@/components/atoms/buttons/StyledButton";
import Squad from "@/models/domain/squad";

export default {
  name: "NoMembersToShowAlert",
  components: {
    StyledButton,
  },
  props: {
    squad: {
      type: Squad,
      required: true,
    },
  },
};
</script>
