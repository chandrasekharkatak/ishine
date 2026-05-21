import { Feature } from 'src/app/models/feature';
import { User } from 'src/app/models/user';

/** Must match feature_master.feature_name in DB (feature_id 81). */
export const INTERVIEW_FEATURE_NAME = 'Interview Tracker';

export interface InterviewUserMapping {
    view_interview: boolean;
    schedule_interview: boolean;
    edit_interview: boolean;
    delete_interview: boolean;
}

export function buildInterviewUserMapping(currentUser: User | null): InterviewUserMapping {
    const mapping: InterviewUserMapping = {
        view_interview: false,
        schedule_interview: false,
        edit_interview: false,
        delete_interview: false
    };
    if (!currentUser?.userMapping) {
        return mapping;
    }
    const featureMap: Feature | undefined = currentUser.userMapping.find(
        (f: Feature) => f.featureName === INTERVIEW_FEATURE_NAME
    );
    featureMap?.subFeatures?.forEach(sub => {
        const key = sub.subFeatureName.replaceAll(' ', '_').toLowerCase();
        mapping[key] = sub.isActive;
    });
    return mapping;
}
